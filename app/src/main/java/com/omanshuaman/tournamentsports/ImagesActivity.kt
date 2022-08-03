package com.omanshuaman.tournamentsports

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.models.Upload
import com.google.firebase.database.*
import com.omanshuaman.tournamentsports.adapters.ImageAdapter


class ImagesActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ImageAdapter? = null
    private var mProgressCircle: ProgressBar? = null
    private var mDatabaseRef: DatabaseReference? = null
    private var mUploads: List<Upload?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        mRecyclerView = findViewById(R.id.recycler_view)
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)

        mUploads = ArrayList()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Tournament").child("Just Photos")
        mDatabaseRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val upload = postSnapshot.getValue(Upload::class.java)
                    (mUploads as ArrayList<Upload?>).add(upload)
                }
                mAdapter = ImageAdapter(this@ImagesActivity, mUploads as ArrayList<Upload?>)
                mRecyclerView!!.adapter = mAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ImagesActivity, databaseError.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}