package com.omanshuaman.tournamentsports

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.omanshuaman.tournamentsports.models.ModelGroupChatList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.omanshuaman.tournamentsports.adapters.AdapterGroupChatList
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class GroupChatsFragment : Fragment() {
    private var groupsRv: RecyclerView? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var groupChatLists: ArrayList<ModelGroupChatList?>? = null
    private var adapterGroupChatList: AdapterGroupChatList? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group_chats, container, false)
        groupsRv = view.findViewById(R.id.groupsRv)
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupChatsList()
        return view
    }

    private fun loadGroupChatsList() {
        groupChatLists = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupChatLists!!.clear()
                for (ds in dataSnapshot.children) {
                    //if current user's uid exists in participants lis of group then show that group
                    if (ds.child("Participants").child(firebaseAuth!!.uid!!).exists()) {
                        val model = ds.getValue(
                            ModelGroupChatList::class.java
                        )
                        groupChatLists!!.add(model)
                    }
                }
                adapterGroupChatList = AdapterGroupChatList(activity!!, groupChatLists!!)
                groupsRv!!.adapter = adapterGroupChatList
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun searchGroupChatsList(query: String) {
        groupChatLists = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupChatLists!!.clear()
                for (ds in dataSnapshot.children) {
                    //if current user's uid exists in participants lis of group then show that group
                    if (ds.child("Participants").child(firebaseAuth!!.uid!!).exists()) {

                        //search by group title
                        if (ds.child("groupTitle").toString().lowercase(Locale.getDefault())
                                .contains(
                                    query.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                        ) {
                            val model = ds.getValue(
                                ModelGroupChatList::class.java
                            )
                            groupChatLists!!.add(model)
                        }
                    }
                }
                adapterGroupChatList = AdapterGroupChatList(activity!!, groupChatLists)
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
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflating menu
        inflater.inflate(R.menu.menu, menu)

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).isVisible = false
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_add_participant).isVisible = false;
        menu.findItem(R.id.action_groupinfo).isVisible = false;

        //SearchView
        val item = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView

        //search listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    //search text contains text, search it
                    searchGroupChatsList(s)
                } else {
                    //search text empty, get all users
                    loadGroupChatsList()
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                //called whenever user press any single letter
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim { it <= ' ' })) {
                    //search text contains text, search it
                    searchGroupChatsList(s)
                } else {
                    //search text empty, get all users
                    loadGroupChatsList()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    /*handle menu item clicks*/
    @Deprecated("Deprecated in Java")
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
            //user not signed in, go to main activity
            startActivity(Intent(activity, SignInActivity::class.java))
            requireActivity().finish()
        }
    }
}