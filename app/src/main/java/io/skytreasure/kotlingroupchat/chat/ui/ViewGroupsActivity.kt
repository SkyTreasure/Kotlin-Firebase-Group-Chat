package io.skytreasure.kotlingroupchat.chat.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_view_groups.*

import io.skytreasure.kotlingroupchat.R
import io.skytreasure.kotlingroupchat.chat.ui.adapter.ViewGroupsAdapter

class ViewGroupsActivity : AppCompatActivity() {

    var adapter : ViewGroupsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_groups)

        rv_main.layoutManager = LinearLayoutManager(this@ViewGroupsActivity) as RecyclerView.LayoutManager?
        adapter = ViewGroupsAdapter(this@ViewGroupsActivity)
        rv_main.adapter = adapter

    }
}
