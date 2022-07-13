package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.R
import com.example.adminuser.models.TrailModel
import com.squareup.picasso.Picasso


class AdapterTrail(context: Context, uploads: List<TrailModel?>?) :
    RecyclerView.Adapter<AdapterTrail.MyHolder>() {
    private val mContext: Context = context
    private val mUploads: List<TrailModel?> = uploads as List<TrailModel?>

    //view holder class
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pImageIv: ImageView
        var pBed: TextView
        var pBedroom: TextView

        init {
            //init views
            pImageIv = itemView.findViewById(R.id.pImageIv)
            pBed = itemView.findViewById(R.id.pBed)
            pBedroom = itemView.findViewById(R.id.pBedroom)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //inflate layout row_post.xml
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.a, parent, false)

        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //get data

        val uBed: String? = mUploads[position]?.bed
        val uBedroom: String? = mUploads[position]?.bedroom
        val pImage: String? = mUploads[position]?.image

        //set data
        holder.pBed.text = uBed
        holder.pBedroom.text = uBedroom

        if (pImage == "noImage") {
            //hide imageview
            holder.pImageIv.visibility = View.GONE
        } else {
            //show imageview
            holder.pImageIv.visibility = View.VISIBLE
            try {
                Picasso.get().load(pImage).into(holder.pImageIv)
            } catch (e: Exception) {
            }
        }

    }

    override fun getItemCount(): Int {
        return mUploads.size
    }
}