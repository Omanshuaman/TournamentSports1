package com.omanshuaman.tournamentsports.fragments


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.adminuser.models.AllModelGroupChatList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.omanshuaman.tournamentsports.GroupCreateActivity
import com.omanshuaman.tournamentsports.MainActivity
import com.omanshuaman.tournamentsports.R;
import com.omanshuaman.tournamentsports.adapters.AdapterAllGroupChatList


class AllGroupFragment : Fragment() {
    private var groupsRv: RecyclerView? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var groupChatLists: ArrayList<AllModelGroupChatList?>? = null
    private var adapterGroupChatList: AdapterAllGroupChatList? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_all_group, container, false)
        groupsRv = view.findViewById(R.id.groupsRv)
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupChatsList()
        return view
    }

    private fun loadGroupChatsList() {
        groupChatLists = ArrayList<AllModelGroupChatList?>()
        val reference = FirebaseDatabase.getInstance().getReference("Groups")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupChatLists!!.clear()
                for (ds in dataSnapshot.children) {
                    //if current user's uid exists in participants lis of group then show that group
                    if (ds.child("Participants").exists()) {
                        val model: AllModelGroupChatList? =
                            ds.getValue(AllModelGroupChatList::class.java)
                        groupChatLists!!.add(model)
                    }
                }
                adapterGroupChatList = AdapterAllGroupChatList(activity!!, groupChatLists!!, firebaseAuth!!
                )
                groupsRv!!.adapter = adapterGroupChatList
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true) //to show menu option in fragment
        super.onCreate(savedInstanceState)
    }

    /*inflate options menu*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflating menu
        inflater.inflate(R.menu.menu, menu)

        //hide add post icon from this fragment
        menu.findItem(R.id.action_add_post).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false
        menu.findItem(R.id.action_groupinfo).isVisible = false

        //SearchView
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView
        super.onCreateOptionsMenu(menu, inflater)
    }

    /*handle menu item clicks*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id
        val id = item.itemId
        if (id == R.id.action_logout) {
            firebaseAuth!!.signOut()
            checkUserStatus()
        } else if (id == R.id.action_create_group) {
            //go to GroupCreateActivity activity
            startActivity(Intent(activity, GroupCreateActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkUserStatus() {
        val user = firebaseAuth!!.currentUser
        if (user == null) {
            //user not signed in, go to main acitivity
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }
    }
}