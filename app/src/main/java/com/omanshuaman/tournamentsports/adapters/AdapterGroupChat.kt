package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import  com.omanshuaman.tournamentsports.R
import com.example.adminuser.models.ModelGroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*


class AdapterGroupChat(
    private val context: Context,
    private val modelGroupChatList: ArrayList<ModelGroupChat?>?
) :
    RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat>() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderGroupChat {
        //inflate layouts
        return if (viewType == MSG_TYPE_right) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false)
            HolderGroupChat(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false)
            HolderGroupChat(view)
        }
    }

    override fun onBindViewHolder(holder: HolderGroupChat, position: Int) {

        //get data
        val model = modelGroupChatList?.get(position)
        val timestamp = model?.timestamp
        val message =
            model?.message //if text message then contain message, if image message then contain url of the image stored in firebase storage
        val senderUid = model?.sender
        val messageType = model!!.type

        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp!!.toLong()
        val dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()

        //set data
        if (messageType.equals("text")) {
            //text message, hide messageIv, show messageTv
            holder.messageIv.visibility = View.GONE
            holder.messageTv.visibility = View.VISIBLE
            holder.messageTv.text = message
        } else {
            //image message, hide messageTv, show messageIv
            holder.messageIv.visibility = View.VISIBLE
            holder.messageTv.visibility = View.GONE
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black)
                    .into(holder.messageIv)
            } catch (e: Exception) {
                holder.messageIv.setImageResource(R.drawable.ic_image_black)
            }
        }
        holder.timeTv.text = dateTime
        setUserName(model, holder)
    }

    private fun setUserName(model: ModelGroupChat, holder: HolderGroupChat) {
        //get sender info from uid in model
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("uid").equalTo(model.sender)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val name = "" + ds.child("name").value
                        holder.nameTv.text = name
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return modelGroupChatList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (modelGroupChatList?.get(position)?.sender == firebaseAuth.uid) {
            MSG_TYPE_right
        } else {
            MSG_TYPE_LEFT
        }
    }

    inner class HolderGroupChat(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView
        val messageTv: TextView
        val timeTv: TextView
        val messageIv: ImageView

        init {
            nameTv = itemView.findViewById(R.id.nameTv)
            messageTv = itemView.findViewById(R.id.messageTv)
            timeTv = itemView.findViewById(R.id.timeTv)
            messageIv = itemView.findViewById(R.id.messageIv)
        }
    }

    companion object {
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_right = 1
    }

}