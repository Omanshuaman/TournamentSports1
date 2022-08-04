package com.omanshuaman.tournamentsports.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.omanshuaman.tournamentsports.GroupCreateActivity
import com.omanshuaman.tournamentsports.R
import com.omanshuaman.tournamentsports.SignInActivity


class HomeFragment : Fragment() {
    //firebase auth
    private var firebaseAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        //init
        firebaseAuth = FirebaseAuth.getInstance()
        return view
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

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        setHasOptionsMenu(true) //to show menu option in fragment
        super.onCreate(savedInstanceState)
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