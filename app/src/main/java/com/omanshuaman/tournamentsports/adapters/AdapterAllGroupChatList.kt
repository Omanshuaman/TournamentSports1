package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.models.AllModelGroupChatList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.R
import com.squareup.picasso.Picasso
import java.util.*



class AdapterAllGroupChatList(
    context: Context,
    groupChatLists: ArrayList<AllModelGroupChatList?>,
    firebaseAuth: FirebaseAuth
) :
    RecyclerView.Adapter<AdapterAllGroupChatList.HolderGroupChatList>() {
    private val context: Context
    private val groupChatLists: ArrayList<AllModelGroupChatList?>
    private val firebaseAuth: FirebaseAuth
    private var myGroupRole: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HolderGroupChatList {

        val view: View = LayoutInflater.from(context).inflate(
            R.layout.allrow_groupchats_list, parent,
            false
        )

        return HolderGroupChatList(view)
    }

    override fun onBindViewHolder(holder: HolderGroupChatList, position: Int) {

        //get data
        val model = groupChatLists[position]
        val groupId = model?.groupId
        val groupIcon = model?.groupIcon
        val groupTitle = model?.groupTitle
        holder.nameTv.text = ""
        holder.timeTv.text = ""
        holder.messageTv.text = ""

        //set data
        holder.groupTitleTv.text = groupTitle
        try {
            Picasso.get().load(groupIcon)
                .placeholder(R.drawable.ic_group_primary)
                .into(holder.groupIconIv)
        } catch (e: Exception) {
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary)
        }

        //handle group click
        holder.itemView.setOnClickListener {
            val myUID: String =
                Objects.requireNonNull(firebaseAuth.currentUser)!!.uid

            /*Check if user already added or not
             * If added: show remove-participant/make-admin/remove-admin option (Admin will not able to change role of creator)
             * If not added, show add participant option*/
            val ref = FirebaseDatabase.getInstance().getReference("Groups")
            ref.child(groupId!!).child("Participants")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            //user exists/participant
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                            //
//                                if (myGroupRole.equals("creator")) {
//                                    Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
//
//                                }
//                                else
                            //user doesn't exists/not-participant: add
                            builder.setTitle("Add Participant")
                                .setMessage("Add this user in this group?")
                                .setPositiveButton(
                                    "ADD"
                                ) { dialog, which -> //add user
                                    addParticipant(myUID, groupId)
                                }
                                .setNegativeButton(
                                    "CANCEL"
                                ) { dialog, which -> dialog.dismiss() }
                                .show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }

    private fun grouprole(groupId: String) {
        val ref1 = FirebaseDatabase.getInstance().getReference("Groups")
        Objects.requireNonNull(firebaseAuth.uid).let {
            if (it != null) {
                ref1.child(groupId).child("Participants").child(it)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                myGroupRole = "" + dataSnapshot.child("role").value
                                Log.d("TAG", "onDataChange: $myGroupRole")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }
    }

    private fun addParticipant(user: String, groupId: String?) {
        //setup user data - add user in group
        val timestamp = "" + System.currentTimeMillis()
        val hashMap: HashMap<String, String> = HashMap()
        hashMap["uid"] = user
        hashMap["role"] = "participant"
        hashMap["timestamp"] = "" + timestamp
        //add that user in Groups>groupId>Participants
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").child(user).setValue(hashMap)
            .addOnSuccessListener { //added successfully
                Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> //failed adding user in group
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return groupChatLists.size
    }

    //view holder class
    inner class HolderGroupChatList(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //ui views
        val groupIconIv: ImageView
        val groupTitleTv: TextView
        val nameTv: TextView
        val messageTv: TextView
        val timeTv: TextView

        init {
            groupIconIv = itemView.findViewById(R.id.groupIconIv)
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv)
            nameTv = itemView.findViewById(R.id.nameTv)
            messageTv = itemView.findViewById(R.id.messageTv)
            timeTv = itemView.findViewById(R.id.timeTv)
        }
    }

    init {
        this.context = context
        this.groupChatLists = groupChatLists
        this.firebaseAuth = firebaseAuth
    }



}