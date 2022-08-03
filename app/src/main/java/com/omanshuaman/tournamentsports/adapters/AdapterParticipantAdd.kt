package com.omanshuaman.tournamentsports.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.R
import com.omanshuaman.tournamentsports.models.ModelUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class AdapterParticipantAdd(
    private val context: Context,
    userList: ArrayList<ModelUser?>?,
    groupId: String,
    myGroupRole: String
) :
    RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd>() {
    private val userList: ArrayList<ModelUser?>?
    private val groupId: String
    private val myGroupRole //creator/admin/participant
            : String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderParticipantAdd {
        //inflate layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_participant_add, parent, false)
        return HolderParticipantAdd(view)
    }

    override fun onBindViewHolder(holder: HolderParticipantAdd, position: Int) {
        //get data
        val modelUser: ModelUser? = userList?.get(position)
        val name: String? = modelUser?.name
        val email: String? = modelUser?.email
        val image: String? = modelUser?.image
        val uid: String? = modelUser?.uid

        //set data
        holder.nameTv.text = name
        holder.emailTv.text = email
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv)
        } catch (e: Exception) {
            holder.avatarIv.setImageResource(R.drawable.ic_default_img)
        }
        if (modelUser != null) {
            checkIfAlreadyExists(modelUser, holder)
        }

        //handle click
        holder.itemView.setOnClickListener {
            /*Check if user already added or not
                     * If added: show remove-participant/make-admin/remove-admin option (Admin will not able to change role of creator)
                     * If not added, show add participant option*/
            val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
            ref.child(groupId).child("Participants").child(uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //user exists/participant
                            val hisPreviousRole = "" + dataSnapshot.child("role").value

                            //options to display in dialog
                            val options: Array<String>
                            val builder = AlertDialog.Builder(
                                context
                            )
                            builder.setTitle("Choose Option")
                            if (myGroupRole == "creator") {
                                if (hisPreviousRole == "admin") {
                                    //im creator, he is admin
                                    options = arrayOf("Remove Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { dialog, which ->
                                        //handle item clicks
                                        if (which == 0) {
                                            //Remove Admin clicked
                                            removeAdmin(modelUser)
                                        } else {
                                            //Remove User clicked
                                            removeParticipant(modelUser)
                                        }
                                    }.show()
                                } else if (hisPreviousRole == "participant") {
                                    //im creator, he is participant
                                    options = arrayOf("Make Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { dialog, which ->
                                        //handle item clicks
                                        if (which == 0) {
                                            //Make Admin clicked
                                            makeAdmin(modelUser)
                                        } else {
                                            //Remove User clicked
                                            removeParticipant(modelUser)
                                        }
                                    }.show()
                                }
                            } else if (myGroupRole == "admin") {
                                if (hisPreviousRole == "creator") {
                                    //im admin, he is creator
                                    Toast.makeText(
                                        context,
                                        "Creator of Group...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (hisPreviousRole == "admin") {
                                    //im admin, he is admin too
                                    options = arrayOf("Remove Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { dialog, which ->
                                        //handle item clicks
                                        if (which == 0) {
                                            //Remove Admin clicked
                                            removeAdmin(modelUser)
                                        } else {
                                            //Remove User clicked
                                            removeParticipant(modelUser)
                                        }
                                    }.show()
                                } else if (hisPreviousRole == "participant") {
                                    //im admin, he is participant
                                    options = arrayOf("Make Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { dialog, which ->
                                        //handle item clicks
                                        if (which == 0) {
                                            //Make Admin clicked
                                            makeAdmin(modelUser)
                                        } else {
                                            //Remove User clicked
                                            removeParticipant(modelUser)
                                        }
                                    }.show()
                                }
                            }
                        } else {
                            //user doesn't exists/not-participant: add
                            val builder = AlertDialog.Builder(
                                context
                            )
                            builder.setTitle("Add Participant")
                                .setMessage("Add this user in this group?")
                                .setPositiveButton(
                                    "ADD"
                                ) { dialog, which -> //add user
                                    addParticipant(modelUser)
                                }
                                .setNegativeButton(
                                    "CANCEL"
                                ) { dialog, which -> dialog.dismiss() }.show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }

    private fun addParticipant(modelUser: ModelUser) {
        //setup user data - add user in group
        val timestamp = "" + System.currentTimeMillis()
        val hashMap = HashMap<String, String>()
        hashMap["uid"] = modelUser.uid!!
        hashMap["role"] = "participant"
        hashMap["timestamp"] = "" + timestamp
        //add that user in Groups>groupId>Participants
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.child(groupId).child("Participants").child(modelUser.uid!!).setValue(hashMap)
            .addOnSuccessListener { //added successfully
                Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> //failed adding user in group
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun makeAdmin(modelUser: ModelUser) {
        //setup data - change role
        val hashMap = HashMap<String, Any>()
        hashMap["role"] = "admin" //roles are: participant/admin/creator
        //update role in db
        val reference = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        reference.child(groupId).child("Participants").child(modelUser.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener { //made admin
                Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> //dailed making admin
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeParticipant(modelUser: ModelUser) {
        //remove participant from group
        val reference = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        reference.child(groupId).child("Participants").child(modelUser.uid!!).removeValue()
            .addOnSuccessListener {
                //removed successfully
            }
            .addOnFailureListener {
                //failed removing participant
            }
    }

    private fun removeAdmin(modelUser: ModelUser) {
        //setup data - remove admin - just change role
        val hashMap = HashMap<String, Any>()
        hashMap["role"] = "participant" //roles are: participant/admin/creator
        //update role in db
        val reference = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        reference.child(groupId).child("Participants").child(modelUser.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener { //made admin
                Toast.makeText(context, "The user is no longer admin...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> //dailed making admin
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfAlreadyExists(modelUser: ModelUser, holder: HolderParticipantAdd) {
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.child(groupId).child("Participants").child(modelUser.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //already exists
                        val hisRole = "" + dataSnapshot.child("role").value
                        holder.statusTv.text = hisRole
                    } else {
                        //doesn't exists
                        holder.statusTv.text = ""
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return userList!!.size
    }

    inner class HolderParticipantAdd(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val avatarIv: ImageView
        val nameTv: TextView
        val emailTv: TextView
        val statusTv: TextView

        init {
            avatarIv = itemView.findViewById(R.id.avatarIv)
            nameTv = itemView.findViewById(R.id.nameTv)
            emailTv = itemView.findViewById(R.id.emailTv)
            statusTv = itemView.findViewById(R.id.statusTv)
        }
    }

    init {
        this.userList = userList
        this.groupId = groupId
        this.myGroupRole = myGroupRole
    }
}