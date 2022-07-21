package com.omanshuaman.tournamentsports

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.omanshuaman.tournamentsports.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.omanshuaman.tournamentsports.databinding.ActivitySignUpBinding


class SignUpActivity : AppCompatActivity() {
    private var binding: ActivitySignUpBinding? = null
    private var mAuth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = ProgressDialog(this@SignUpActivity)
        progressDialog!!.setTitle("Creating Account")
        progressDialog!!.setMessage("We're creating your account")

        binding!!.btnSignUp.setOnClickListener {
            if (!binding!!.txtUsername.text.toString()
                    .isEmpty() && !binding!!.txtEmail.text.toString().isEmpty()
                && !binding!!.txtPassword.text.toString().isEmpty()
            ) {
                progressDialog!!.show()
                mAuth!!.createUserWithEmailAndPassword(
                    binding!!.txtEmail.text.toString(),
                    binding!!.txtPassword.text.toString()
                )
                    .addOnCompleteListener { task ->
                        progressDialog!!.dismiss()
                        if (task.isSuccessful) {

                            // Sign in success, dismiss dialog and start register activity
                            progressDialog!!.dismiss()
                            val user = mAuth!!.currentUser                    //if user is signing in first time then get and show user info from google account
                            //Get user email and uid from auth
                            val users = ModelUser()
                            users.email = user!!.email
                            users.uid = user.uid
                            //When user is registered store user info in firebase realtime database too
                            //using HashMap
                            val hashMap: HashMap<Any, String?> = HashMap()
                            //put info in hashmap
                            hashMap["email"] = users.email
                            hashMap["uid"] = users.uid
                            hashMap["name"] = "" //will add later (e.g. edit profile)
                            hashMap["image"] = "" //will add later (e.g. edit profile)
                            hashMap["cover"] = "" //will add later (e.g. edit profile)
                            //firebase database instance
                            val database = FirebaseDatabase.getInstance()
                            //path to store user data named "Users"
                            val reference = database.getReference("Users")
                            //put data within hashmap in database
                            reference.child(user.uid).setValue(hashMap)

                            startActivity(
                                Intent(
                                    this@SignUpActivity,
                                    SignInActivity::class.java
                                )
                            )


//                            val user = Users(
//                                binding!!.txtUsername.text.toString(),
//                                binding!!.txtEmail.text.toString(),
//                                binding!!.txtPassword.text.toString()
//                            )
//                            val id = task.result.user?.uid
//                            if (id != null) {
//                                database!!.reference.child("Users").child(id).setValue(user)
//                            }
                            Toast.makeText(
                                this@SignUpActivity,
                                "Sign Up Successful",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                task.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignUpActivity, "Enter Credentials", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding!!.txtAlreadyHaveAccount.setOnClickListener {
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}