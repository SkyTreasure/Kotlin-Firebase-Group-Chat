package io.skytreasure.kotlingroupchat.login

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

import io.skytreasure.kotlingroupchat.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*;
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import android.support.annotation.NonNull
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.MainActivity
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.constants.PrefConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SecurePrefs
import io.skytreasure.kotlingroupchat.common.util.SharedPrefManager


class LoginActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 9001
    private var mAuth: FirebaseAuth? = null
    private var prevUser: FirebaseUser? = null
    var currentUser: FirebaseUser? = null
    var mUserModel: UserModel? = UserModel()
    val progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        MyChatManager.init(this@LoginActivity)
        btn_google.setOnClickListener(this)
        setupGoogleSignIn()
        sCurrentUser = SharedPrefManager.getInstance(this@LoginActivity).savedUserModel
        progressDialog?.show()

        /**
         * If user has already logged in then pass the saved usermodel to loginCreateAndUpdate
         * method.
         */
        if (sCurrentUser != null) {
            MyChatManager.loginCreateAndUpdate(object : NotifyMeInterface {
                override fun handleData(`object`: Any, requestCode: Int?) {
                    progressDialog?.hide()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }, sCurrentUser, NetworkConstants.LOGIN_REQUEST)


        } else {
            progressDialog?.hide()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btn_google -> GooglesignIn()
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }


    private fun setupGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun GooglesignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                var account = result.getSignInAccount()
                firebaseAuthWithGoogle(account!!)
            } else {
                Toast.makeText(this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Get an  token for the signed-in user, exchange it for a Firebase credential,
     * and authenticate with Firebase using the Firebase credential
     *
     * @param acct
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = mAuth?.getCurrentUser()
                currentUser = user
                mUserModel?.uid = user?.uid!!
                mUserModel?.name = user?.displayName!!
                mUserModel?.email = user?.email!!
                mUserModel?.image_url = user?.photoUrl.toString()
                mUserModel?.online = true
                SharedPrefManager.getInstance(this@LoginActivity).savePreferences(PrefConstants.USER_DATA, Gson().toJson(mUserModel))
                SecurePrefs(this@LoginActivity).put(PrefConstants.USER_ID, user?.uid!!)
                SecurePrefs(this@LoginActivity).put(PrefConstants.USER_EMAIL, user?.email!!)
                firebaseLogin()
            } else {

                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception!!.message == getString(R.string.user_exists)) {
                    prevUser = currentUser
                    mUserModel?.uid = currentUser?.uid!!
                    mUserModel?.name = currentUser?.displayName!!
                    mUserModel?.email = currentUser?.email!!
                    mUserModel?.image_url = currentUser?.photoUrl.toString()
                    mUserModel?.online = true
                    SecurePrefs(this@LoginActivity).put(PrefConstants.USER_ID, currentUser?.uid!!)
                    SecurePrefs(this@LoginActivity).put(PrefConstants.USER_EMAIL, currentUser?.email!!)
                    SharedPrefManager.getInstance(this@LoginActivity).savePreferences(PrefConstants.USER_DATA, Gson().toJson(mUserModel))
                    linkWithExistingUser(credential)
                } else {
                    Toast.makeText(this@LoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    /**
     * @param credential
     */
    private fun linkWithExistingUser(credential: AuthCredential) {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance()
        }
        prevUser?.linkWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "linkWithCredential:success")
                val user = task.result.user
                firebaseLogin()
            } else {
                Log.w(TAG, "linkWithCredential:failure", task.exception)
                Toast.makeText(this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Checks whether userid is present in firebase or not, if not then it updates firebase.
     */
    private fun firebaseLogin() {
        MyChatManager.loginCreateAndUpdate(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                val i = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }, mUserModel!!, NetworkConstants.LOGIN_REQUEST)

    }

    override fun onStop() {
        super.onStop()

    }


}
