package com.omanshuaman.tournamentsports


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.adminuser.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.omanshuaman.tournamentsports.adapters.AdapterParticipantAdd
import com.squareup.picasso.Picasso
import java.util.*


class GroupInfoActivity : AppCompatActivity() {
    private var groupId: String? = null
    private var myGroupRole = ""
    private var firebaseAuth: FirebaseAuth? = null
    private var actionBar: ActionBar? = null

    //ui views
    private var groupIconIv: ImageView? = null
    private var descriptionTv: TextView? = null
    private var createdByTv: TextView? = null
    private var editGroupTv: TextView? = null
    private var addParticipantTv: TextView? = null
    private var leaveGroupTv: TextView? = null
    private var participantsTv: TextView? = null
    private var participantsRv: RecyclerView? = null
    private var userList: ArrayList<ModelUser?>? = null
    private var adapterParticipantAdd: AdapterParticipantAdd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        groupIconIv = findViewById(R.id.groupIconIv)
        descriptionTv = findViewById(R.id.descriptionTv)
        createdByTv = findViewById(R.id.createdByTv)
        editGroupTv = findViewById(R.id.editGroupTv)
        addParticipantTv = findViewById(R.id.addParticipantTv)
        leaveGroupTv = findViewById(R.id.leaveGroupTv)
        participantsTv = findViewById(R.id.participantsTv)
        participantsRv = findViewById(R.id.participantsRv)

        groupId = intent.getStringExtra("groupId")
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupInfo()
        loadMyGroupRole()

        addParticipantTv!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@GroupInfoActivity, GroupParticipantAddActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        })
        editGroupTv!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@GroupInfoActivity, GroupEditActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        })

        leaveGroupTv!!.setOnClickListener(View.OnClickListener {
            //if user is participant/admin: leave group
            //if user is creator: delete group
            var dialogTitle = ""
            var dialogDescription = ""
            var positiveButtonTitle = ""
            if (myGroupRole == "creator") {
                dialogTitle = "Delete Group"
                dialogDescription = "Are you sure you want to Delete group permanently?"
                positiveButtonTitle = "DELETE"
            } else {
                dialogTitle = "Leave Group"
                dialogDescription = "Are you sure you want to Leave group permanently?"
                positiveButtonTitle = "LEAVE"
            }
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@GroupInfoActivity)
            builder.setTitle(dialogTitle)
                .setMessage(dialogDescription)
                .setPositiveButton(positiveButtonTitle,
                    DialogInterface.OnClickListener { dialog, which ->
                        if (myGroupRole == "creator") {
                            //im creator of group: delete group
                            deleteGroup()
                        } else {
                            //im participant/admin: leave group
                            leaveGroup()
                        }
                    })
                .setNegativeButton("CANCEL",
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                .show()
        })

    }

    private fun leaveGroup() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").child(firebaseAuth!!.uid!!)
            .removeValue()
            .addOnSuccessListener { //group left successfully...
                Toast.makeText(
                    this@GroupInfoActivity,
                    "Group left successfully...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                startActivity(Intent(this@GroupInfoActivity, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e -> //failed to leave group
                Toast.makeText(this@GroupInfoActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteGroup() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!)
            .removeValue()
            .addOnSuccessListener { //group deleted successfully...
                Toast.makeText(
                    this@GroupInfoActivity,
                    "Group successfully deleted...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                startActivity(Intent(this@GroupInfoActivity, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e -> //failed to delete group
                Toast.makeText(this@GroupInfoActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGroupInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        //get group info
                        val groupId = "" + ds.child("groupId").value
                        val groupTitle = "" + ds.child("groupTitle").value
                        val groupDescription = "" + ds.child("groupDescription").value
                        val groupIcon = "" + ds.child("groupIcon").value
                        val createdBy = "" + ds.child("createdBy").value
                        val timestamp = "" + ds.child("timestamp").value
                        val cal = Calendar.getInstance(Locale.ENGLISH)
                        cal.timeInMillis = timestamp.toLong()
                        val dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()
                        loadCreatorInfo(dateTime, createdBy)

                        //set group info
                        actionBar!!.setTitle(groupTitle)
                        descriptionTv!!.text = groupDescription
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary)
                                .into(groupIconIv)
                        } catch (e: Exception) {
                            groupIconIv!!.setImageResource(R.drawable.ic_group_primary)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun loadCreatorInfo(dateTime: String, createBy: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("uid").equalTo(createBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val name = "" + ds.child("name").value
                        createdByTv!!.text = "Created by $name on $dateTime"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun loadMyGroupRole() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").orderByChild("uid")
            .equalTo(firebaseAuth!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        myGroupRole = "" + ds.child("role").value
                        actionBar!!.setSubtitle(firebaseAuth!!.currentUser!!.email + " (" + myGroupRole + ")")
                        if (myGroupRole == "participant") {
                            editGroupTv!!.visibility = View.GONE
                            addParticipantTv!!.visibility = View.GONE
                            leaveGroupTv!!.text = "Leave Group"
                        } else if (myGroupRole == "admin") {
                            editGroupTv!!.visibility = View.GONE
                            addParticipantTv!!.visibility = View.VISIBLE
                            leaveGroupTv!!.text = "Leave Group"
                        } else if (myGroupRole == "creator") {
                            editGroupTv!!.visibility = View.VISIBLE
                            addParticipantTv!!.visibility = View.VISIBLE
                            leaveGroupTv!!.text = "Delete Group"
                        }
                    }
                    loadParticipants()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun loadParticipants() {
        userList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList!!.clear()
                    for (ds in dataSnapshot.children) {
                        //get uid from Group > Participants
                        val uid = "" + ds.child("uid").value

                        //get info of user using uid we got above
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.orderByChild("uid").equalTo(uid)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (ds in dataSnapshot.children) {
                                        val modelUser = ds.getValue(ModelUser::class.java)
                                        userList!!.add(modelUser)
                                    }
                                    //adapter
                                    adapterParticipantAdd = AdapterParticipantAdd(
                                        this@GroupInfoActivity, userList,
                                        groupId!!, myGroupRole
                                    )
                                    //set adapter
                                    participantsRv!!.adapter = adapterParticipantAdd
                                    participantsTv!!.text = "Participants (" + userList!!.size + ")"
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