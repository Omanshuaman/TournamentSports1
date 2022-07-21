package com.omanshuaman.tournamentsports

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RecyclerActivity : AppCompatActivity() {
    private var mTextview: TextView? = null
    private var Id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        mTextview = findViewById(R.id.textview_recycler)

        //get id of the group
        val intent = intent
        Id = intent.getStringExtra("Id")

        val ref = FirebaseDatabase.getInstance().getReference("Just Photos")
        ref.orderByChild("id").equalTo(Id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val groupTitle = "" + ds.child("name").value
                        mTextview!!.text = groupTitle
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }
}