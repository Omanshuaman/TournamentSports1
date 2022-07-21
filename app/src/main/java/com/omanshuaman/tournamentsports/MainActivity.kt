package com.omanshuaman.tournamentsports

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.adevinta.leku.LATITUDE
import com.adevinta.leku.LONGITUDE
import com.adevinta.leku.LocationPickerActivity
import com.adevinta.leku.locale.SearchZoneRect
import com.omanshuaman.tournamentsports.models.LocationModel
import com.omanshuaman.tournamentsports.models.Upload
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    //widgets
    private var uploadBtn: Button? = null
    private var showAllBtn: Button? = null
    private var imageView: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var mEditTextFileName: EditText? = null
    private var btnPicklocation: Button? = null
    private var tvMylocation: TextView? = null
    private val PLACE_PICKER_REQUEST2 = 999

    private var latitude: String? = null
    private var longitude: String? = null

    private val userid = FirebaseAuth.getInstance().currentUser!!.uid

    //vars
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference =
        FirebaseStorage.getInstance().reference.child("Photos").child(userid)
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uploadBtn = findViewById(R.id.upload_btn)
        showAllBtn = findViewById(R.id.showall_btn)
        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)
        mEditTextFileName = findViewById(R.id.Name)
        btnPicklocation = findViewById(R.id.BtnPickLocation)
        tvMylocation = findViewById(R.id.MyLocation)

        progressBar!!.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()

        imageView!!.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, 2)
        }

        btnPicklocation!!.setOnClickListener {
            openPlacePicker()
        }

        uploadBtn!!.setOnClickListener {
            if (imageUri != null) {
                uploadToFirebase(imageUri!!)
            } else {
                Toast.makeText(this@MainActivity, "Please Select Image", Toast.LENGTH_SHORT).show()
            }
        }
        showAllBtn!!.setOnClickListener {
            val intent = Intent(this, GroupCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openPlacePicker() {
        val locationPickerIntent = LocationPickerActivity.Builder()
            .withLocation(41.4036299, 2.1743558)
            .withGeolocApiKey("<PUT API KEY HERE>")
            .withSearchZone("es_ES")
            .withSearchZone(
                SearchZoneRect(
                    LatLng(26.525467, -18.910366),
                    LatLng(43.906271, 5.394197)
                )
            )
            .withDefaultLocaleSearchZone()
            .shouldReturnOkOnBackPressed()
            .withStreetHidden()
            .withCityHidden()
            .withZipCodeHidden()
            .withSatelliteViewHidden()
            //.withGooglePlacesEnabled()
            .withGoogleTimeZoneEnabled()
            .withVoiceSearchHidden()
            .withUnnamedRoadHidden()
            .withMapStyle(R.raw.map_style)
            // .withSearchBarHidden()
            .build(applicationContext)


        startActivityForResult(locationPickerIntent, PLACE_PICKER_REQUEST2)
    }

    private fun openImagesActivity() {
        val intent = Intent(this, ImagesActivity::class.java)
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageView!!.setImageURI(imageUri)
        } else
            if (resultCode == Activity.RESULT_OK && data != null) {
                Log.d("RESULT****", "OK")
                if (requestCode == PLACE_PICKER_REQUEST2) {

                    latitude = data.getDoubleExtra(LATITUDE, 0.0).toString()
                    Log.d("LATITUDE****", latitude.toString())
                    longitude = data.getDoubleExtra(LONGITUDE, 0.0).toString()
                    Log.d("LONGITUDE****", longitude.toString())

                    val sb = StringBuilder()
                    sb.append("LATITUDE:").append(latitude).append("\n").append("LONGITUDE: ")
                        .append(longitude)
                    tvMylocation?.text = sb.toString()
                }
            }
    }

    private fun uploadToFirebase(uri: Uri) {
        val fileRef =
            storageReference.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(uri)
            )
        val g_timestamp = "" + System.currentTimeMillis()

        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri1: Uri ->
                val model = Upload(g_timestamp,
                    mEditTextFileName!!.text.toString(),
                    uri1.toString(),
                    longitude,
                    latitude
                )
                val locationModel = LocationModel(longitude,latitude)
                val modelId = databaseReference.push().key

                databaseReference.child("Image").child(userid).child(modelId!!)
                    .setValue(model)
                databaseReference.child("Just Photos").child(g_timestamp).setValue(model)
                databaseReference.child("Location").child(modelId).child("LatLng").setValue(locationModel)

                progressBar!!.visibility = View.INVISIBLE
                Toast.makeText(this@MainActivity, "Uploaded Successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnProgressListener { progressBar!!.visibility = View.VISIBLE }.addOnFailureListener {
            progressBar!!.visibility = View.INVISIBLE
            Toast.makeText(this@MainActivity, "Uploading Failed !!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(mUri: Uri): String? {
        val cr = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(mUri))
    }

}
