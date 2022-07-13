package com.omanshuaman.tournamentsports

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.omanshuaman.tournamentsports.fragments.AllGroupFragment
import com.omanshuaman.tournamentsports.fragments.HomeFragment
import com.omanshuaman.tournamentsports.fragments.NotificationsFragment
import com.omanshuaman.tournamentsports.fragments.UsersFragment


class DashboardActivity : AppCompatActivity() {
    //firebase auth
    var firebaseAuth: FirebaseAuth? = null
    var actionBar: ActionBar? = null
    private var navigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        actionBar = supportActionBar
        actionBar?.title = "Profile"

        //init
        firebaseAuth = FirebaseAuth.getInstance()
        //bottom navigation
        navigationView = findViewById(R.id.navigation)

        navigationView!!.setOnNavigationItemSelectedListener(selectedListener)

        //home fragment transaction (default, on star)
        actionBar?.title = "Home" //change actionbar title
        val fragment1 = HomeFragment()
        val ft1: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft1.replace(R.id.content, fragment1, "")
        ft1.commit()
    }

    private val selectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem -> //handle item clicks
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    //home fragment transaction
                    actionBar?.title = "Home" //change actionbar title
                    val fragment1 = MapFragment()
                    val ft1: FragmentTransaction = supportFragmentManager.beginTransaction()
                    ft1.replace(R.id.content, fragment1, "")
                    ft1.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.trail_fragment -> {
                    //profile fragment transaction
                    actionBar?.title = "Profile" //change actionbar title
                    val fragment2 = TrailFragment()
                    val ft2: FragmentTransaction = supportFragmentManager.beginTransaction()
                    ft2.replace(R.id.content, fragment2, "")
                    ft2.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_users -> {
                    //users fragment transaction
                    actionBar?.title = "Users" //change actionbar title
                    val fragment3 = UsersFragment()
                    val ft3: FragmentTransaction = supportFragmentManager.beginTransaction()
                    ft3.replace(R.id.content, fragment3, "")
                    ft3.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_more -> {
                    showMoreOptions()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun showMoreOptions() {
        //popup menu to show more options
        //  PopupMenu popup = new PopupMenu(getActivity(), menuItemView);
        val popupMenu = navigationView?.let { PopupMenu(this, it, Gravity.END) }
        //items to show in menu
        popupMenu?.menu?.add(Menu.NONE, 0, 0, "Notifications")
        popupMenu?.menu?.add(Menu.NONE, 1, 0, "Group Chats")
        popupMenu?.menu?.add(Menu.NONE, 2, 0, "All Group Chats");


        //menu clicks
        popupMenu?.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                //notifications clicked

                //Notifications fragment transaction
                actionBar!!.title = "Notifications" //change actionbar title
                val fragment5 = NotificationsFragment()
                val ft5 = supportFragmentManager.beginTransaction()
                ft5.replace(R.id.content, fragment5, "")
                ft5.commit()
            } else if (id == 1) {
                //group chats clicked

                //Notifications fragment transaction
                actionBar!!.title = "Group Chats" //change actionbar title
                val fragment6 = GroupChatsFragment()
                val ft6 = supportFragmentManager.beginTransaction()
                ft6.replace(R.id.content, fragment6, "")
                ft6.commit()
            } else if (id == 2) {
                //group chats clicked

                //Notifications fragment transaction
                actionBar?.title = "All Group Chats";//change actionbar title
                val fragment7 = AllGroupFragment();
                val ft7 = supportFragmentManager.beginTransaction()

                ft7.replace(R.id.content, fragment7, "");
                ft7.commit();
            }
            false
        }
        popupMenu?.show()
    }

    private fun checkUserStatus() {
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
        } else {
            //user not signed in, go to main activity
            startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        //check on start of app
        checkUserStatus()
        super.onStart()
    }

}