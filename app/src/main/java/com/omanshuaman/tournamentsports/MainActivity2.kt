package com.omanshuaman.tournamentsports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Initialize fragment
        // Initialize fragment
        val fragment: Fragment = MapFragment()

        // Open fragment
        supportFragmentManager
            .beginTransaction().replace(R.id.frame_layout,fragment)
            .commit();
    }
}