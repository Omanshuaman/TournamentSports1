package com.omanshuaman.tournamentsports

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")

class GroupCreateActivity : AppCompatActivity() {
    //permission arrays
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private var image_uri: Uri? = null
    private var actionBar: ActionBar? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var groupIconIv: ImageView? = null
    private var groupTitleEt: EditText? = null
    private var groupDescriptionEt: EditText? = null
    private var createGroupBtn: FloatingActionButton? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_create)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar!!.title = "Create Group"

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv)
        groupTitleEt = findViewById(R.id.groupTitleEt)
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt)
        createGroupBtn = findViewById(R.id.createGroupBtn)

        //init permission arrays
        cameraPermissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //pick image
        groupIconIv!!.setOnClickListener { showImagePickDialog() }

        //handle click event
        createGroupBtn!!.setOnClickListener { startCreatingGroup() }
    }

    private fun startCreatingGroup() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Creating Group")

        //input title,description
        val groupTitle = groupTitleEt!!.text.toString().trim { it <= ' ' }
        val groupDescription = groupDescriptionEt!!.text.toString().trim { it <= ' ' }
        //validation
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Please enter group title...", Toast.LENGTH_SHORT).show()
            return  //don't proceed further
        }
        progressDialog!!.show()

        //timestamp: for groupicon image, groupId, timeCreated etc
        val g_timestamp = "" + System.currentTimeMillis()
        if (image_uri == null) {
            //creating group without icon image
            createGroup(
                "" + g_timestamp,
                "" + groupTitle,
                "" + groupDescription,
                ""
            )
        } else {
            //creating group with icon image
            //upload image
            //image name and path
            val fileNameAndPath = "Group_Imgs/image$g_timestamp"
            val storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath)
            storageReference.putFile(image_uri!!)
                .addOnSuccessListener { taskSnapshot -> //image uploaded, get url
                    val p_uriTask = taskSnapshot.storage.downloadUrl
                    while (!p_uriTask.isSuccessful);
                    val p_downloadUri = p_uriTask.result
                    if (p_uriTask.isSuccessful) {
                        createGroup(
                            "" + g_timestamp,
                            "" + groupTitle,
                            "" + groupDescription,
                            "" + p_downloadUri
                        )
                    }
                }
                .addOnFailureListener { e -> //failed uploading image
                    progressDialog!!.dismiss()
                    Toast.makeText(this@GroupCreateActivity, "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        }
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    private fun createGroup(
        g_timestamp: String,
        groupTitle: String,
        groupDescription: String,
        groupIcon: String
    ) {
        //setup info of group
        val hashMap = HashMap<String, String>()
        hashMap["groupId"] = "" + g_timestamp
        hashMap["groupTitle"] = "" + groupTitle
        hashMap["groupDescription"] = "" + groupDescription
        hashMap["groupIcon"] = "" + groupIcon
        hashMap["timestamp"] = "" + g_timestamp
        hashMap["createdBy"] = "" + firebaseAuth!!.uid

        //create group
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(g_timestamp).setValue(hashMap)
            .addOnSuccessListener {
                //created successfully

                //setup member inf (add current user in group's participants list)
                val hashMap1 = HashMap<String, String?>()
                hashMap1["uid"] = firebaseAuth!!.uid
                hashMap1["role"] = "creator" //roles are creator, admin, participant
                hashMap1["timestamp"] = g_timestamp
                val ref1 = FirebaseDatabase.getInstance().getReference("Groups")
                ref1.child(g_timestamp).child("Participants").child(firebaseAuth!!.uid!!)
                    .setValue(hashMap1)
                    .addOnSuccessListener { //participant added
                        progressDialog!!.dismiss()
                        Toast.makeText(
                            this@GroupCreateActivity,
                            "Group created...",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener { e -> //failed adding participant
                        progressDialog!!.dismiss()
                        Toast.makeText(this@GroupCreateActivity, "" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            .addOnFailureListener { e -> //failed
                progressDialog!!.dismiss()
                Toast.makeText(this@GroupCreateActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImagePickDialog() {
        //options to pick image from
        val options = arrayOf("Camera", "Gallery")
        //dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image:")
            .setItems(options) { dialog, which ->
                //handle clicks
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermissions()) {
                        requestCameraPermissions()
                    } else {
                        pickFromCamera()
                    }
                } else {
                    //gallery clicked
                    if (!checkStoragePermissions()) {
                        requestStoragePermissions()
                    } else {
                        pickFromGallery()
                    }
                }
            }.show()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickFromCamera() {
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title")
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun checkStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    private fun checkUser() {
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            actionBar!!.subtitle = user.email
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        //handle permission result
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera()
                    } else {
                        //both or one is denied
                        Toast.makeText(
                            this,
                            "Camera & Storage permissions are required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery()
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage permissions required", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //handle image pick result
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //was picked from gallery
                image_uri = data!!.data
                //set to imageview
                groupIconIv!!.setImageURI(image_uri)
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //was picked from camera
                //set to imageview
                groupIconIv!!.setImageURI(image_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        //permission constants
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 200

        //image pick constants
        private const val IMAGE_PICK_CAMERA_CODE = 300
        private const val IMAGE_PICK_GALLERY_CODE = 400
    }
}