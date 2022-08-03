package com.omanshuaman.tournamentsports

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.models.ParticipantInfo

@Suppress("DEPRECATION")

class ParticipantInfoActivity : AppCompatActivity() {

    private var uploadBtn: FloatingActionButton? = null
    private var myourname: EditText? = null
    private var mPhoneNumber: EditText? = null
    private var progressDialog: ProgressDialog? = null
    private var Id: String? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participant_info)

        uploadBtn = findViewById(R.id.upload)
        myourname = findViewById(R.id.your_name)
        mPhoneNumber = findViewById(R.id.phone_number)
        firebaseAuth = FirebaseAuth.getInstance()

        //get id of the group
        val intent = intent
        Id = intent.getStringExtra("groupId1")

        uploadBtn!!.setOnClickListener {
            uploadToFirebase()
//                val intent = Intent(this, GroupCreateActivity::class.java)
//                startActivity(intent)

        }
    }

    private fun uploadToFirebase() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Uploading")

        val gTimestamp = "" + System.currentTimeMillis()
        progressDialog!!.show()

        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Just Photos")

        ref.orderByChild("id").equalTo(Id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val model = ParticipantInfo(
                            myourname!!.text.toString(), mPhoneNumber?.text.toString()
                        )
                        ref.child(Id!!).child("Participant info").child(firebaseAuth?.uid!!)
                            .child(Id!!)
                            .setValue(model)

                        Toast.makeText(
                            this@ParticipantInfoActivity,
                            "Uploaded Successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        progressDialog!!.dismiss()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })


    }

}
