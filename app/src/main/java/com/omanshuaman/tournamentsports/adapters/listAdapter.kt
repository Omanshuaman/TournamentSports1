package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.R
import com.omanshuaman.tournamentsports.models.ListModel
import com.squareup.picasso.Picasso

class listAdapter(var context: Context, var postList: ArrayList<ListModel?>) :
    RecyclerView.Adapter<listAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //inflate layout row_post.xml
        val view = LayoutInflater.from(context).inflate(R.layout.activity_list, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, i: Int) {
        //get data
        val uid = postList[i]?.id
        val uBed = postList[i]?.bed
        val uBedroom = postList[i]?.bedroom
        val uImage = postList[i]?.image
        val pOldprice = postList[i]?.oldprice
        val pTitle = postList[i]?.title
        val ptotalprice = postList[i]?.totalPrice
        val type = postList[i]?.type
        try {
            Picasso.get().load(uImage).into(MyHolder.uImageView)
        } catch (e: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    //view holder class
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //views from row_post.xml
        var pBed: TextView
        var pBedroom: TextView
        var pDescription: TextView
        var pOld_price: TextView
        var pNew_price: TextView
        var pTotal_price: TextView? = null

        companion object {
            lateinit var uImageView: ImageView
        }

        init {

            //init views
            uImageView = itemView.findViewById(R.id.pImageView)
            pBed = itemView.findViewById(R.id.pBed)
            pBedroom = itemView.findViewById(R.id.pBedroom)
            pDescription = itemView.findViewById(R.id.pDescription)
            pOld_price = itemView.findViewById(R.id.pOld_price)
            pNew_price = itemView.findViewById(R.id.pNew_price)
        }
    }
}