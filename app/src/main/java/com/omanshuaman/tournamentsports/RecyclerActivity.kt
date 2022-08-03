package com.omanshuaman.tournamentsports

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RecyclerActivity : AppCompatActivity() {
    private var mTextview: TextView? = null
    private var Id: String? = null
    private var mButton: Button? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        mTextview = findViewById(R.id.textview_recycler)
        mButton = findViewById(R.id.regiter)
        firebaseAuth = FirebaseAuth.getInstance()

        //get id of the group
        val intent = intent
        Id = intent.getStringExtra("Id")

        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Just Photos")
        ref.orderByChild("id").equalTo(Id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val groupTitle = "" + ds.child("tournamentName").value
                        val uid = firebaseAuth!!.uid
                        mTextview!!.text = groupTitle
                        Log.d(
                            "TAGc", "onDataChange: " + ds.child(Id!!)
                                .child("Participant info")
                                .child(uid!!).key.equals(firebaseAuth!!.uid)
                        )
                        if ((ds.child("uid").value!! == uid) || (ds.child(Id!!)
                                .child("Participant info")
                                .child(uid).key.equals(firebaseAuth!!.uid))
                        ) {
                            mButton!!.isEnabled = false
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        mButton!!.setOnClickListener {
            val intent1 = Intent(this, ParticipantInfoActivity::class.java)
            intent1.putExtra("groupId1", Id)
            startActivity(intent1)
        }
    }
}