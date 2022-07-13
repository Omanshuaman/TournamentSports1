package com.omanshuaman.tournamentsports

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminuser.models.ListModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.adapters.listAdapter


class ListActivity() : AppCompatActivity() {

    //firebase auth
    var firebaseAuth: FirebaseAuth? = null

    var recyclerView: RecyclerView? = null
    var postList: List<ListModel>? = null
    var adapterPosts: listAdapter? = null


    private var line_through: TextView? = null
    private var under_line: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        //init post list
        //init post list
        postList = ArrayList()

        loadPosts()
//        line_through = findViewById(R.id.strike_trhough)
//        under_line = findViewById(R.id.underline)
//        line_through!!.text = "$36"
//        line_through!!.paintFlags = line_through!!.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//
//        under_line!!.text = "\$230 total"
//        under_line!!.paintFlags = under_line!!.paintFlags or Paint.UNDERLINE_TEXT_FLAG
//
//        val image: ImageView = findViewById<View>(R.id.imageView) as ImageView
//        image.clipToOutline = true
//        try {
//            Picasso.get().load(imageUrl).into(image)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
    }
    private fun loadPosts() {
        //path of all posts
        val ref = FirebaseDatabase.getInstance().getReference("users")
        //get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val modelList= ds.getValue(ListModel::class.java)
                    (postList as ArrayList<ListModel?>).add(modelList)

                    //adapter
                    adapterPosts = listAdapter(this@ListActivity, postList as ArrayList<ListModel?>)
                    //set adapter to recyclerview
                    recyclerView!!.adapter = adapterPosts
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //in case of error
                Toast.makeText(this@ListActivity, "" + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

