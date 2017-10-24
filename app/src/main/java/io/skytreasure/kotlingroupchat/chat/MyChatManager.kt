package io.skytreasure.kotlingroupchat.chat

import android.content.Context
import android.support.compat.BuildConfig
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import io.skytreasure.kotlingroupchat.chat.model.GroupModel
import io.skytreasure.kotlingroupchat.chat.model.UserModel
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.PrefConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import io.skytreasure.kotlingroupchat.common.util.SecurePrefs

/**
 * Created by akash on 23/10/17.
 */
object MyChatManager {

    val TAG = "MyChatManager"
    var sMyChatManager: MyChatManager? = null
    var sAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    var sDatabase: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var isFirebaseAuthSuccessfull = false
    var firebaseUserId = ""
    var mFirebaseDatabaseReference: DatabaseReference? = FirebaseDatabase.getInstance().reference
    val gson = Gson()
    var mContext: Context? = null
    var mUserRef: DatabaseReference? = mFirebaseDatabaseReference?.child(FirebaseConstants.USERS)
    var mGroupRef: DatabaseReference? = mFirebaseDatabaseReference?.child(FirebaseConstants.GROUP)


    fun setmContext(mContext: Context) {
        this.mContext = mContext

    }

    fun init(mContext: Context) {
        this.mContext = mContext
        setupFirebaseAuth()
        signInToFirebaseAnonymously()
    }

    /**
     * Setup Firebase Auth and Database
     */
    fun setupFirebaseAuth() {
        if (sAuth == null)
            sAuth = FirebaseAuth.getInstance()
        if (sDatabase == null)
            sDatabase = FirebaseDatabase.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                firebaseUserId = user.uid
                isFirebaseAuthSuccessfull = false
                signInToFirebaseAnonymously()
            }
        }
    }

    /**
     * Sign in to firebase Anonymously
     */
    fun signInToFirebaseAnonymously() {
        setupFirebaseAuth()
        if (!isFirebaseAuthSuccessfull) {
            sAuth?.signInAnonymously()?.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("", "signInAnonymously", task.exception)
                    isFirebaseAuthSuccessfull = false
                } else {
                    isFirebaseAuthSuccessfull = true
                }
            }?.addOnFailureListener { Log.w("", "signInAnonymously") }
        }

    }

    /**
     * Login
     */
    fun loginCreateAndUpdate(callback: NotifyMeInterface?, userModel: UserModel?, requestType: Int?) {
        try {
            mUserRef?.child(userModel?.uid)?.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val p = mutableData.getValue<UserModel>(UserModel::class.java)
                    if (p == null) {
                        mUserRef?.child(userModel?.uid)?.setValue(userModel)
                    } else {
                        mutableData.setValue(userModel)
                    }
                    return Transaction.success(mutableData)

                }

                override fun onComplete(databaseError: DatabaseError?, p1: Boolean, dataSnapshot: DataSnapshot?) {
                    try {
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError)
                        callback?.handleData(true, requestType)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Get user list from firebase
     */
    fun getAllUsersFromFirebase(callback: NotifyMeInterface?, requestType: Int?) {

        // Making a copy of listener
        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(dataSnaphot: DataSnapshot) {
                if (dataSnaphot.exists()) {
                    var userList: ArrayList<UserModel> = ArrayList()
                    /*dataSnaphot.getValue<UserModel>(UserModel::class.java)?.let {
                        userList.add(it)
                    }*/
                    dataSnaphot.children.forEach { it ->
                        it.getValue<UserModel>(UserModel::class.java)?.let {
                            if (!SecurePrefs(mContext!!).get(PrefConstants.USER_ID).equals(it.uid)) {
                                userList.add(it)
                            }
                        }
                    }
                    callback?.handleData(userList, requestType)
                }
            }
        }

        mUserRef?.addListenerForSingleValueEvent(listener)

    }

    /**
     * This function creates a group in the firebase and adds an entry of group id under users and set it to
     * true.
     */
    fun createGroup(callback: NotifyMeInterface?, group: GroupModel, requestType: Int?) {

        val groupId = mGroupRef?.push()?.key
        group.groupId = groupId

        for (user in group.members) {
            user.value.groups = hashMapOf()
            user.value.email = null
            user.value.image_url = null
            user.value.name = null
        }

        mGroupRef?.child(groupId)?.setValue(group)

        for (user in group.members) {
            mUserRef?.child(user.value.uid)?.child(FirebaseConstants.GROUP)?.child(groupId)?.setValue(true)
        }




        callback?.handleData(true, requestType)
    }


}