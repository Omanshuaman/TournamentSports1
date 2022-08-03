package com.omanshuaman.tournamentsports.adapters

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.models.Upload
import com.omanshuaman.tournamentsports.R
import com.squareup.picasso.Picasso


class ImageAdapter(context: Context, uploads: List<Upload?>?) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val mContext: Context = context
    private val mUploads: List<Upload?> = uploads as List<Upload?>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(v)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uploadCurrent = mUploads[position]
        holder.textViewName.text = uploadCurrent?.tournamentName
        Picasso.get()
            .load(uploadCurrent?.imageUrl)
            .fit()
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mUploads.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        var imageView: ImageView = itemView.findViewById(R.id.image_view_upload)

    }

}