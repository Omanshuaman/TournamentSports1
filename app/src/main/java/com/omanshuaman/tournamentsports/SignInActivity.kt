package com.omanshuaman.tournamentsports

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.omanshuaman.tournamentsports.databinding.ActivitySignInBinding
import com.omanshuaman.tournamentsports.models.ModelUser


class SignInActivity : AppCompatActivity() {

    private var binding: ActivitySignInBinding? = null
    private var progressDialog: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this@SignInActivity)
        progressDialog!!.setTitle("Login")
        progressDialog!!.setMessage("Please Wait \n Validation in Progress")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("319310297783-pi682lf0pgvgr28rcv45dlr6pima6oa2.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        binding!!.btnSignIn.setOnClickListener {
            if (binding!!.txtEmail.text.toString()
                    .isNotEmpty() && binding!!.txtPassword.text.toString()
                    .isNotEmpty()
            ) {
                progressDialog!!.show()
                mAuth!!.signInWithEmailAndPassword(
                    binding!!.txtEmail.text.toString(),
                    binding!!.txtPassword.text.toString()
                )
                    .addOnCompleteListener { task ->
                        progressDialog!!.dismiss()
                        if (task.isSuccessful) {
                            val intent = Intent(this@SignInActivity, DashboardActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@SignInActivity,
                                task.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignInActivity, "Enter Credentials", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (mAuth!!.currentUser != null) {
            val intent = Intent(this@SignInActivity, DashboardActivity::class.java)
            startActivity(intent)
        }
        binding!!.txtClickSignUp.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding!!.btnGoogle.setOnClickListener { signIn() }
    }

    private var RC_SIGN_IN = 9001

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result

                firebaseAuthWithGoogle(account)

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.w("SignIn", "signInWithCredential:success")

                    // Sign in success, update UI with the signed-in user's information
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

                    //show user email in toast
                    Toast.makeText(this@SignInActivity, "" + user.email, Toast.LENGTH_SHORT)
                        .show()
                    //go to profile activity after logged in
                    startActivity(Intent(this@SignInActivity, DashboardActivity::class.java))
                    finish()
                    //updateUI(user);

                    Toast.makeText(this@SignInActivity, "Sign in with Google", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.w("TAGmm", "signInWithCredential:failure", task.exception)

                    Toast.makeText(this@SignInActivity, "Sorry error auth", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}