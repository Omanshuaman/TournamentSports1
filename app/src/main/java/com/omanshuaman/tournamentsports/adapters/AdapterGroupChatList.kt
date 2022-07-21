package com.omanshuaman.tournamentsports.adapters



import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.GroupChatActivity
import com.omanshuaman.tournamentsports.models.ModelGroupChatList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.R
import com.squareup.picasso.Picasso
import java.util.*


class AdapterGroupChatList(
    private val context: Context,
    private val groupChatLists: ArrayList<ModelGroupChatList?>?
) : RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderGroupChatList {
        //inflate layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list, parent, false)
        return HolderGroupChatList(view)
    }

    override fun onBindViewHolder(holder: HolderGroupChatList, position: Int) {

        //get data
        val model = groupChatLists?.get(position)
        val groupId = model?.groupId
        val groupIcon = model?.groupIcon
        val groupTitle = model?.groupTitle

        holder.nameTv.text = ""
        holder.timeTv.text = ""
        holder.messageTv.text = ""

        //load last message and message-time
        if (model != null) {
            loadLastMessage(model, holder)
        }

        //set data
        holder.groupTitleTv.text = groupTitle
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary)
                .into(holder.groupIconIv)
        } catch (e: Exception) {
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary)
        }

        //handle group click
        holder.itemView.setOnClickListener {
            //open group chat
            //open group chat
            val intent = Intent(context, GroupChatActivity::class.java)
            intent.putExtra("groupId", groupId)
            context.startActivity(intent)
        }
    }

    private fun loadLastMessage(model: ModelGroupChatList, holder: HolderGroupChatList) {
        //get last message from group
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(model.groupId!!).child("Messages")
            .limitToLast(1) //get last item(message) from that child
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {

                        //get data
                        val message = "" + ds.child("message").value
                        val timestamp = "" + ds.child("timestamp").value
                        val sender = "" + ds.child("sender").value
                        val messageType = "" + ds.child("type").value

                        //convert time
                        //convert time stamp to dd/mm/yyyy hh:mm am/pm
                        val cal = Calendar.getInstance(Locale.ENGLISH)
                        cal.timeInMillis = timestamp.toLong()
                        val dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()
                        if (messageType == "image") {
                            holder.messageTv.text = "Sent Photo"
                        } else {
                            holder.messageTv.text = message
                        }
                        holder.timeTv.text = dateTime

                        //get info of sender of last message
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.orderByChild("uid").equalTo(sender)
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
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return groupChatLists!!.size
    }

    //view holder class
    inner class HolderGroupChatList(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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
}