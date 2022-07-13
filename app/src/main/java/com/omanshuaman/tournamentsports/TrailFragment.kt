package com.omanshuaman.tournamentsports

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminuser.models.ModelGroupChat
import com.example.adminuser.models.TrailModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omanshuaman.tournamentsports.adapters.AdapterTrail


class TrailFragment : Fragment() {
    private var firebaseAuth: FirebaseAuth? = null


    var recyclerView: RecyclerView? = null
    var postList: ArrayList<TrailModel>? = null
    var adapterPosts: AdapterTrail? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        setHasOptionsMenu(true) //to show menu option in fragment
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_trail, container, false)

        firebaseAuth = FirebaseAuth.getInstance();
        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerview)
        val layoutManager = LinearLayoutManager(activity)
        //set layout to recyclerview
        recyclerView!!.layoutManager = layoutManager

        loadPosts()
        // Inflate the layout for this fragment
        return view
    }

    private fun loadPosts() {
        //path of all posts
        //init post list
        postList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("users")
        //get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList!!.clear()
                for (ds in dataSnapshot.children) {
                    val modelList = ds.getValue(TrailModel::class.java)
                    if (modelList != null) {
                        postList!!.add(modelList)
                    }
                    Log.d("Yes", "onDataChange: " + modelList.toString())
                    Log.d("Yes", "onDataChange: " + postList.toString())

                    //adapter
                    adapterPosts = activity?.let { AdapterTrail(it, postList) }
                    //set adapter to recyclerview
                    recyclerView!!.adapter = adapterPosts
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //in case of error
                Toast.makeText(activity, "" + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(activity, SignInActivity::class.java))
            requireActivity().finish()
        }
    }

    /*inflate options menu*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflating menu
        inflater.inflate(R.menu.menu, menu)
        //hide some options
        menu.findItem(R.id.action_create_group).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false;
        menu.findItem(R.id.action_groupinfo).isVisible = false;

        super.onCreateOptionsMenu(menu, inflater)
    }

    /*handle menu item clicks*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id
        val id: Int = item.itemId
        if (id == R.id.logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        } else if (id == R.id.action_create_group) {
            //go to GroupCreateActivity activity
            startActivity(Intent(activity, GroupCreateActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}