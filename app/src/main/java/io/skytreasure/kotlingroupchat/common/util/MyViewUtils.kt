package io.skytreasure.kotlingroupchat.common.util

/**
 * Created by akash on 23/10/17.
 */

import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import io.skytreasure.kotlingroupchat.R
import java.util.*

/**
 * Created by akash on 15/6/17.
 */
class MyViewUtils(val mContext: Context) {

    companion object {

        @BindingAdapter("loadImageFromUrl")
        fun loadImageFromUrl(view: AppCompatImageView, url: String) {
            view.buildDrawingCache()
            Glide.with(view.context)
                    .load(url)
                    .placeholder(R.drawable.pattern_placeholder)
                    .into(view)
        }

        @BindingAdapter("loadImageFromUrl")
        fun loadImageBitmapFromUrl(view: AppCompatImageView, url: String) {
            view.buildDrawingCache()
            Glide.with(view.context)
                    .load(url)
                    .asBitmap()
                    .placeholder(R.drawable.pattern_placeholder)
                    .into(view)
        }

        fun setNineBySixteenHeight(context: Context, view: View) {
            val display = (context as AppCompatActivity).windowManager.defaultDisplay
            val width = display.width
            val height = width * 9 / 16
            view.layoutParams.height = height
        }

        fun patternTest(dateInputPattern: String,
                        dateString: String,
                        dateTargetPattern: String, tv: TextView) {
            val sdf = java.text.SimpleDateFormat(dateInputPattern, Locale.getDefault())
            val date = sdf.parse(dateString)
            sdf.applyPattern(dateTargetPattern)
            tv.text = sdf.format(date)
        }



        fun patternTest(dateInputPattern: String,
                        dateString: String,
                        dateTargetPattern: String): String {
            val sdf = java.text.SimpleDateFormat(dateInputPattern, Locale.getDefault())
            val date = sdf.parse(dateString)
            sdf.applyPattern(dateTargetPattern)
            return sdf.format(date)
        }

        fun patternTestCheck(dateInputPattern: String,
                             dateString: String,
                             dateTargetPattern: String): Boolean {
            val sdf = java.text.SimpleDateFormat(dateInputPattern, Locale.getDefault())

            return try {
                val date = sdf.parse(dateString)
                sdf.applyPattern(dateTargetPattern)
                true
            } catch (e: Exception) {
                false
            }

        }

    }

}

@BindingAdapter("LoadRoundImage")
fun loadRoundImage(view: AppCompatImageView, imageUrl: String) {

    Glide.with(view.context)
            .load(imageUrl)
            .asBitmap().centerCrop()
            .placeholder(R.drawable.default_user)
            .into(object : BitmapImageViewTarget(view) {
                override fun setResource(resource: Bitmap) {
                    val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(view.context.resources, resource)
                    circularBitmapDrawable.isCircular = true
                    view.setImageDrawable(circularBitmapDrawable)
                }
            })
}