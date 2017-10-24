package io.skytreasure.kotlingroupchat.chat.ui

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.MyChatManager
import io.skytreasure.kotlingroupchat.chat.model.MessageModel
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ChatMessagesRecyclerAdapter
import io.skytreasure.kotlingroupchat.common.constants.AppConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.groupMembersMap
import io.skytreasure.kotlingroupchat.common.constants.DataConstants.Companion.sCurrentUser
import io.skytreasure.kotlingroupchat.common.constants.NetworkConstants
import io.skytreasure.kotlingroupchat.common.controller.NotifyMeInterface
import kotlinx.android.synthetic.main.activity_chat_messages.*
import java.util.*

class ChatMessagesActivity : AppCompatActivity(), View.OnClickListener {


    var adapter: ChatMessagesRecyclerAdapter? = null
    var groupId: String? = ""
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_messages)

        MyChatManager.setmContext(this@ChatMessagesActivity)

        groupId = intent.getStringExtra(AppConstants.GROUP_ID)

        chat_messages_recycler.layoutManager = LinearLayoutManager(this@ChatMessagesActivity) as RecyclerView.LayoutManager?

        progressDialog?.show()
        btnSend.setOnClickListener(this)
        iv_back.setOnClickListener(this)
        chat_room_title.setOnClickListener(this)

        MyChatManager.fetchGroupMembersDetails(object : NotifyMeInterface {
            override fun handleData(`object`: Any, requestCode: Int?) {
                adapter = ChatMessagesRecyclerAdapter(groupId!!, this@ChatMessagesActivity)
                chat_messages_recycler.adapter = adapter
                progressDialog?.hide()
            }

        }, NetworkConstants.FETCH_GROUP_MEMBERS_DETAILS, groupId)

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

        for (member in groupMembersMap?.get(groupId)!!) {
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

            }

        }, NetworkConstants.SEND_MESSAGE_REQUEST, groupId, messageModel)
    }
}
