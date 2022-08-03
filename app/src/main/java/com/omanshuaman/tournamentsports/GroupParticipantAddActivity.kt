package com.omanshuaman.tournamentsports

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.adapters.AdapterParticipantAdd


class GroupParticipantAddActivity : AppCompatActivity() {
    private var usersRv: RecyclerView? = null
    private var actionBar: ActionBar? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var groupId: String? = null
    private var myGroupRole: String? = null
    private var userList: ArrayList<ModelUser?>? = null
    private var adapterParticipantAdd: AdapterParticipantAdd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_participant_add)
        actionBar = supportActionBar
        actionBar!!.title = "Add Participants"
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        firebaseAuth = FirebaseAuth.getInstance()

        //init views
        usersRv = findViewById(R.id.usersRv)
        groupId = intent.getStringExtra("groupId")
        loadGroupInfo()
    }//not my uid

    //setup adapter
    //set adapter to recyclerview
//get all users accept currently signed in
    //init list
    //load users from db
    private val allUsers: Unit
        private get() {
            //init list
            userList = ArrayList()
            //load users from db
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList!!.clear()
                    for (ds in dataSnapshot.children) {
                        val modelUser = ds.getValue(ModelUser::class.java)

                        //get all users accept currently signed in
                        if (firebaseAuth!!.uid != modelUser!!.uid) {
                            //not my uid
                            userList!!.add(modelUser)
                        }
                    }
                    //setup adapter
                    adapterParticipantAdd = AdapterParticipantAdd(
                        this@GroupParticipantAddActivity,
                        userList,
                        "" + groupId,
                        "" + myGroupRole
                    )
                    //set adapter to recyclerview
                    usersRv!!.adapter = adapterParticipantAdd
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun loadGroupInfo() {
        val ref1 = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val groupId = "" + ds.child("groupId").value
                        val groupTitle = "" + ds.child("groupTitle").value
                        val groupDescription = "" + ds.child("groupDescription").value
                        val groupIcon = "" + ds.child("groupIcon").value
                        val createdBy = "" + ds.child("createdBy").value
                        val timestamp = "" + ds.child("timestamp").value
                        actionBar!!.setSubtitle("Add Participants")
                        ref1.child(groupId).child("Participants").child(firebaseAuth!!.uid!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        myGroupRole = "" + dataSnapshot.child("role").value
                                        actionBar!!.setTitle("$groupTitle($myGroupRole)")
                                        allUsers
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}