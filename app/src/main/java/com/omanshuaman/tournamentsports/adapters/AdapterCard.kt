
package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.GroupChatActivity
import com.omanshuaman.tournamentsports.R
import com.example.adminuser.models.Upload
import com.google.firebase.database.FirebaseDatabase
import com.omanshuaman.tournamentsports.RecyclerActivity


class AdapterCard(context: Context, uploads: List<Upload?>?) :
    RecyclerView.Adapter<AdapterCard.MyHolder>() {
    private val mContext: Context = context
    private val mUploads: List<Upload?> = uploads as List<Upload?>

    //view holder classA
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView

        init {
            name = itemView.findViewById(R.id.txtPlaceName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //inflate layout row_post.xml
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.place_layout, parent, false)

        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data

        val model = mUploads.get(position)
        val id = model?.Id

        val name: String? = mUploads[position]?.name

        //set data
        holder.name.text = name

        //handle group click
        holder.itemView.setOnClickListener {
            //open group chat
            //open group chat
            val intent = Intent(mContext, RecyclerActivity::class.java)
            intent.putExtra("Id", id)
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return mUploads.size
    }
}