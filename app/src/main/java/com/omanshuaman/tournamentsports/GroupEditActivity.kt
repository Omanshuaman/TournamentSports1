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
import android.text.format.DateFormat
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*


class GroupEditActivity : AppCompatActivity() {
    //permission arrays
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    //picked image uri
    private var image_uri: Uri? = null
    private var actionBar: ActionBar? = null

    //firebae auth
    private var firebaseAuth: FirebaseAuth? = null

    //UI views
    private var groupIconIv: ImageView? = null
    private var groupTitleEt: EditText? = null
    private var groupDescriptionEt: EditText? = null
    private var updateGroupBtn: FloatingActionButton? = null
    private var progressDialog: ProgressDialog? = null
    private var groupId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_edit)
        actionBar = supportActionBar
        actionBar!!.title = "Edit Group"
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        //init UI views
        groupIconIv = findViewById(R.id.groupIconIv)
        groupTitleEt = findViewById(R.id.entry_fee)
        groupDescriptionEt = findViewById(R.id.prize_money)
        updateGroupBtn = findViewById(R.id.updateGroupBtn)
        groupId = intent.getStringExtra("groupId")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        //init permission arrays
        cameraPermissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadGroupInfo()

        //pick image
        groupIconIv!!.setOnClickListener(View.OnClickListener { showImagePickDialog() })

        //handle click event
        updateGroupBtn!!.setOnClickListener(View.OnClickListener { startUpdatingGroup() })
    }

    private fun startUpdatingGroup() {
        //input data
        val groupTitle = groupTitleEt!!.text.toString().trim { it <= ' ' }
        val groupDescription = groupDescriptionEt!!.text.toString().trim { it <= ' ' }
        //validate data
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Group title is required...", Toast.LENGTH_SHORT).show()
            return
        }
        progressDialog!!.setMessage("Updating Group Info...")
        progressDialog!!.show()
        if (image_uri == null) {
            //update group without icon
            val hashMap = HashMap<String, Any>()
            hashMap["groupTitle"] = groupTitle
            hashMap["groupDescription"] = groupDescription
            val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
            ref.child(groupId!!).updateChildren(hashMap)
                .addOnSuccessListener { //updated...
                    progressDialog!!.dismiss()
                    Toast.makeText(
                        this@GroupEditActivity,
                        "Group info updated...",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                .addOnFailureListener { e -> //update failed
                    progressDialog!!.dismiss()
                    Toast.makeText(this@GroupEditActivity, "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            //update group with icon
            val timestamp = "" + System.currentTimeMillis()
            val filePathAndName = "Group_Imgs/image_$timestamp"

            //upload image to firebase storage
            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(image_uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    //image uploaded
                    //get url
                    val p_uriTask = taskSnapshot.storage.downloadUrl
                    while (!p_uriTask.isSuccessful);
                    val p_downloadUri = p_uriTask.result
                    if (p_uriTask.isSuccessful) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["groupTitle"] = groupTitle
                        hashMap["groupDescription"] = groupDescription
                        hashMap["groupIcon"] = "" + p_downloadUri
                        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
                        ref.child(groupId!!).updateChildren(hashMap)
                            .addOnSuccessListener { //updated...
                                progressDialog!!.dismiss()
                                Toast.makeText(
                                    this@GroupEditActivity,
                                    "Group info updated...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e -> //update failed
                                progressDialog!!.dismiss()
                                Toast.makeText(
                                    this@GroupEditActivity,
                                    "" + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e -> //image upload failed
                    progressDialog!!.dismiss()
                    Toast.makeText(this@GroupEditActivity, "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun loadGroupInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        //get group info
                        val groupId = "" + ds.child("groupId").value
                        val groupTitle = "" + ds.child("groupTitle").value
                        val groupDescription = "" + ds.child("groupDescription").value
                        val groupIcon = "" + ds.child("groupIcon").value
                        val createdBy = "" + ds.child("createdBy").value
                        val timestamp = "" + ds.child("timestamp").value
                        val cal = Calendar.getInstance(Locale.ENGLISH)
                        cal.timeInMillis = timestamp.toLong()
                        val dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()


                        //set group info
                        groupTitleEt!!.setText(groupTitle)
                        groupDescriptionEt!!.setText(groupDescription)
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary)
                                .into(groupIconIv)
                        } catch (e: Exception) {
                            groupIconIv!!.setImageResource(R.drawable.ic_group_primary)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
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

    @Deprecated("Deprecated in Java")
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