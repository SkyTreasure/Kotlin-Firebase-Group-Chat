package io.skytreasure.kotlingroupchat.chat.ui

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ChatMessagesRecyclerAdapter
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sMyGroups
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.FirebaseConstants
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import kotlinx.android.synthetic.main.activity_chat_messages.*
import java.util.*

class ChatMessagesActivity : AppCompatActivity(), View.OnClickListener {


    var adapter: ChatMessagesRecyclerAdapter? = null
    var groupId: String? = ""
    var position: Int? = 0
    var progressDialog: ProgressDialog? = null
    var mFirebaseDatabaseReference: DatabaseReference? = null
    var mLinearLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_messages)

        groupId = intent.getStringExtra(AppConstants.GROUP_ID)
        position = intent.getIntExtra(AppConstants.POSITION, 1)

        MyChatManager.setmContext(this@ChatMessagesActivity)


        chat_room_title.text = sMyGroups?.get(position!!)?.name


        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager!!.setStackFromEnd(true)
        //  chat_messages_recycler.layoutManager = mLinearLayoutManager

        progressDialog?.show()
        btnSend.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        chat_room_title.setOnClickListener(this)

        MyChatManager.fetchGroupMembersDetails(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                readMessagesFromFirebase(groupId!!)
                getLastMessageAndUpdateUnreadCount()

            }

        }, NetworkConstants.FETCH_GROUP_MEMBERS_DETAILS, groupId)


    }

    fun getLastMessageAndUpdateUnreadCount() {
        MyChatManager.fetchLastMessageFromGroup(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                var lastMessage: MessageModel? = `object` as MessageModel
                if (lastMessage != null) {
                    MyChatManager.updateUnReadCountLastSeenMessageTimestamp(groupId, lastMessage!!)
                }

            }

        }, NetworkConstants.FETCH_MESSAGES, groupId)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                var message: String = et_chat.text.toString()
                if (!message.isEmpty()) {
                    sendMessage(message!!)
                }
            }

            R.id.iv_back -> {
                finish()
            }

            R.id.chat_room_title -> {

            }
        }
    }

    fun sendMessage(message: String) {
        var cal: Calendar = Calendar.getInstance()


        var read_status_temp: HashMap<String, Boolean> = hashMapOf()

        for (member in groupMembersMap?.get(groupId!!)!!) {
            if (member.uid == sCurrentUser?.uid) {
                read_status_temp.put(member.uid!!, true)
            } else {
                read_status_temp.put(member.uid!!, false)
            }
        }

        var messageModel: MessageModel? = MessageModel(message, sCurrentUser?.uid, cal.timeInMillis.toString(),
                read_status = read_status_temp)

        MyChatManager.sendMessageToAGroup(object : NotifyMeInterface {

            override fun handleData(`object`: Any, requestCode: Int?) {
                et_chat.setText("")
            }

        }, NetworkConstants.SEND_MESSAGE_REQUEST, groupId, messageModel)
    }

    override fun onStop() {
        super.onStop()
        getLastMessageAndUpdateUnreadCount()
    }


    private fun readMessagesFromFirebase(groupId: String) {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val firebaseAdapter = ChatMessagesRecyclerAdapter(groupId, this@ChatMessagesActivity, mFirebaseDatabaseReference?.child(FirebaseConstants.MESSAGES)?.child(groupId)!!)

        firebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = firebaseAdapter.getItemCount()
                val lastVisiblePosition = mLinearLayoutManager?.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    chat_messages_recycler.scrollToPosition(positionStart)
                }
            }
        })
        chat_messages_recycler.setLayoutManager(mLinearLayoutManager)
        chat_messages_recycler.setAdapter(firebaseAdapter)
    }
}
