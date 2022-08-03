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
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.omanshuaman.tournamentsports.adapters.AdapterGroupChat
import com.omanshuaman.tournamentsports.models.ModelGroupChat
import com.squareup.picasso.Picasso


class GroupChatActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    private var groupId: String? = null
    private var myGroupRole = ""
    private var toolbar: Toolbar? = null
    private var groupIconIv: ImageView? = null
    private var attachBtn: ImageButton? = null
    private var sendBtn: ImageButton? = null
    private var groupTitleTv: TextView? = null
    private var messageEt: EditText? = null
    private var chatRv: RecyclerView? = null
    private var groupChatList: ArrayList<ModelGroupChat?>? = null
    private var adapterGroupChat: AdapterGroupChat? = null

    //permissions to be requested
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    //uri of picked image
    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        //init views
        toolbar = findViewById(R.id.toolbar)
        groupIconIv = findViewById(R.id.groupIconIv)
        groupTitleTv = findViewById(R.id.groupTitleTv)
        attachBtn = findViewById(R.id.attachBtn)
        messageEt = findViewById(R.id.messageEt)
        sendBtn = findViewById(R.id.sendBtn)
        chatRv = findViewById(R.id.chatRv)

        setSupportActionBar(toolbar)

        //get id of the group
        val intent = intent
        groupId = intent.getStringExtra("groupId")

        //init required permissions
        cameraPermission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        //init required permissions
        //init required permissions
        cameraPermission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupInfo()
        loadGroupMessages()
        loadMyGroupRole()

        sendBtn!!.setOnClickListener {
            //input data
            val message = messageEt?.text.toString().trim { it <= ' ' }
            //validate
            if (TextUtils.isEmpty(message)) {
                //empty, don't send
                Toast.makeText(
                    this@GroupChatActivity,
                    "Can't send empty message...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //send message
                sendMessage(message)
            }
        }
        attachBtn!!.setOnClickListener { //pick image from camera/gallery
            showImageImportDialog()
        }
    }

    private fun showImageImportDialog() {
        //options to display
        val options = arrayOf("Camera", "Gallery")

        //dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image")
            .setItems(options) { dialog, which ->
                //handle clicks
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        //not granted, request
                        requestCameraPermission()
                    } else {
                        //already granted
                        pickCamera()
                    }
                } else {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        //not granted, request
                        requestStoragePermission()
                    } else {
                        //already granted
                        pickGallery()
                    }
                }
            }
            .show()
    }

    private fun pickGallery() {
        //intent to pick image fro gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription")
        image_uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
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

    private fun sendImageMessage() {
        //progress dialog
        val pd = ProgressDialog(this)
        pd.setTitle("Please wait")
        pd.setMessage("Sending Image...")
        pd.setCanceledOnTouchOutside(false)
        pd.show()

        //file name and path in firebase storage
        val filenamePath = "ChatImages/" + "" + System.currentTimeMillis()
        val storageReference = FirebaseStorage.getInstance().getReference(filenamePath)
        //upload image
        storageReference.putFile(image_uri!!)
            .addOnSuccessListener { taskSnapshot ->
                //image uploaded, get url
                val p_uriTask = taskSnapshot.storage.downloadUrl
                while (!p_uriTask.isSuccessful);
                val p_downloadUri = p_uriTask.result
                if (p_uriTask.isSuccessful) {
                    //image url received, save in db

                    //timestamp
                    val timestamp = "" + System.currentTimeMillis()

                    //setup message data
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["sender"] = "" + firebaseAuth!!.uid
                    hashMap["message"] = "" + p_downloadUri
                    hashMap["timestamp"] = "" + timestamp
                    hashMap["type"] = "" + "image" //text/image/file

                    //add in db
                    val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
                    ref.child(groupId!!).child("Messages").child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener { //message sent
                            //clear messageEt
                            messageEt!!.setText("")
                            pd.dismiss()
                        }
                        .addOnFailureListener { e ->
                            pd.dismiss()
                            //message sending failed
                            Toast.makeText(
                                this@GroupChatActivity,
                                "" + e.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
            }
            .addOnFailureListener { e -> //failed uploading image
                Toast.makeText(this@GroupChatActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                pd.dismiss()
            }
    }

    private fun loadMyGroupRole() {
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.child(groupId!!).child("Participants")
            .orderByChild("uid").equalTo(firebaseAuth!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        myGroupRole = "" + ds.child("role").value
                        //refresh menu items
                        invalidateOptionsMenu()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun loadGroupMessages() {
        //init list
        groupChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.child(groupId!!).child("Messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    groupChatList!!.clear()
                    for (ds in dataSnapshot.children) {
                        val model: ModelGroupChat? = ds.getValue(ModelGroupChat::class.java)
                        groupChatList!!.add(model)
                    }
                    //adapter
                    adapterGroupChat = AdapterGroupChat(this@GroupChatActivity, groupChatList)
                    //set to recyclerview
                    chatRv!!.adapter = adapterGroupChat
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun sendMessage(message: String) {

        //timestamp
        val timestamp = "" + System.currentTimeMillis()

        //setup message data
        val hashMap = HashMap<String, Any>()
        hashMap["sender"] = "" + firebaseAuth!!.uid
        hashMap["message"] = "" + message
        hashMap["timestamp"] = "" + timestamp
        hashMap["type"] = "" + "text" //text/image/file

        //add in db
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.child(groupId!!).child("Messages").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener { //message sent
                //clear messageEt
                messageEt!!.setText("")
            }
            .addOnFailureListener { e -> //message sending failed
                Toast.makeText(this@GroupChatActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGroupInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Tournament").child("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val groupTitle = "" + ds.child("groupTitle").value
                        val groupDescription = "" + ds.child("groupDescription").value
                        val groupIcon = "" + ds.child("groupIcon").value
                        val timestamp = "" + ds.child("timestamp").value
                        val createdBy = "" + ds.child("createdBy").value
                        groupTitleTv!!.text = groupTitle
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white)
                                .into(groupIconIv)
                        } catch (e: Exception) {
                            groupIconIv!!.setImageResource(R.drawable.ic_group_white)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_create_group).isVisible = false
        menu.findItem(R.id.action_add_post).isVisible = false
        menu.findItem(R.id.action_logout).isVisible = false
        menu.findItem(R.id.action_search).isVisible = false

        menu.findItem(R.id.action_add_participant).isVisible = myGroupRole == "creator" || myGroupRole == "admin"
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.action_add_participant) {
            val intent = Intent(this, GroupParticipantAddActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
        else if (id == R.id.action_groupinfo){
            val intent = Intent(this, GroupInfoActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //picked from gallery
                image_uri = data?.data
                sendImageMessage()
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //picked from camera
                sendImageMessage()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted && writeStorageAccepted) {
                    pickCamera()
                } else {
                    Toast.makeText(
                        this,
                        "Camera & Storage permissions are required...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            STORAGE_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeStorageAccepted) {
                    pickGallery()
                } else {
                    Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    companion object {
        //permission request constants
        private const val CAMERA_REQUEST_CODE = 200
        private const val STORAGE_REQUEST_CODE = 400

        //image pick constants
        private const val IMAGE_PICK_GALLERY_CODE = 1000
        private const val IMAGE_PICK_CAMERA_CODE = 2000
    }
}