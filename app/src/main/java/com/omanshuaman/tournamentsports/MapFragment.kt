package com.omanshuaman.tournamentsports

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.omanshuaman.tournamentsports.adapters.AdapterCard
import com.omanshuaman.tournamentsports.models.Upload


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var button: Button? = null
    var recyclerView: RecyclerView? = null
    var markerList: ArrayList<Upload>? = null
    var adapterCard: AdapterCard? = null
    private var mMarkerArray = ArrayList<Marker?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        button = view.findViewById(R.id.button1)

        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        supportMapFragment!!.getMapAsync(this)
        recyclerView = view.findViewById(R.id.placesRecyclerView)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        //set layout to recyclerview

        recyclerView!!.layoutManager = layoutManager

        marker()

        return view
    }

    private fun marker() {
        val helper: SnapHelper = LinearSnapHelper()

        markerList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Just Photos")
        //get all data from this ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                markerList!!.clear()
                for (ds in dataSnapshot.children) {
                    val modelList = ds.getValue(Upload::class.java)
                    if (modelList != null) {
                        markerList!!.add(modelList)
                    }
                    //adapter
                    adapterCard = activity?.let { AdapterCard(it, markerList) }
                    //set adapter to recyclerview
                    recyclerView!!.adapter = adapterCard

//                    recyclerView!!.addItemDecoration(object :
//                        DividerItemDecoration(context, LinearLayoutManager.VERTICAL) {
//                        override fun onDraw(
//                            c: Canvas,
//                            parent: RecyclerView,
//                            state: RecyclerView.State
//                        ) {
//                        }
//                    })

                    helper.attachToRecyclerView(recyclerView)
//                    snapHelper.attachToRecyclerView(recyclerView)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //in case of error
                Toast.makeText(activity, "" + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Location")


        mMap = googleMap
        mMarkerArray = ArrayList()

        button?.setOnClickListener {
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                for (child in dataSnapshot.children) {
                    val long =
                        dataSnapshot.child("LatLng").child("longitude").value.toString().toDouble()
                    val lat =
                        dataSnapshot.child("LatLng").child("latitude").value.toString().toDouble()

                    Log.d("LONG", "onChildAdded: " + long.toString().toDouble())
                    val location = LatLng(lat, long)
                    val marker: Marker? = googleMap.addMarker(
                        MarkerOptions().position(location)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    mMarkerArray.add(marker)

                }
                Log.d("Array", "onChildAdded: " + mMarkerArray.size)

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                Log.d("Data onChildChanged", dataSnapshot.value.toString())
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("Data onChildRemoved", dataSnapshot.value.toString())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                Log.d("Data onChildMoved", dataSnapshot.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        mMap.setOnMarkerClickListener { marker ->
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            val markerPosition = marker.position
            var selectedMarker = 0
            for (i in 0 until mMarkerArray.size) {

                if (markerPosition.latitude == mMarkerArray[i]?.position?.latitude && markerPosition.longitude == mMarkerArray[i]?.position?.longitude
                ) {
                    selectedMarker = i
                }
            }
            val cameraPosition = CameraPosition.Builder().target(markerPosition).zoom(12f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            recyclerView?.smoothScrollToPosition(selectedMarker)
            // marker.showInfoWindow()
            return@setOnMarkerClickListener false
        }


        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                val firstChild: View = recyclerView.getChildAt(0)
//                val topHolder =
//                    recyclerView.getChildViewHolder(firstChild)
//                Log.d("TAG9", "onScrolled: " + topHolder.absoluteAdapterPosition)

                val lastChild: View =
                    recyclerView.getChildAt(recyclerView.childCount - 1)
                val bottomHolder =
                    recyclerView.getChildViewHolder(lastChild)
                Log.d("TAG10", "onScrolled: $bottomHolder")


                for (i in 0 until mMarkerArray.size) {

                    Log.d("56", "onScrolled: $i")
                    if (i == bottomHolder.absoluteAdapterPosition
                    ) {

                        val cameraPosition = CameraPosition.Builder().target(
                            LatLng(
                                mMarkerArray[i]!!.position.latitude,
                                mMarkerArray[i]!!.position.longitude,
                            )
                        ).zoom(12f).build()
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                    }
                }

            }

        })

    }


}