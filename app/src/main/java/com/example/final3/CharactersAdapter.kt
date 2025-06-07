package com.example.final3

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class CharactersAdapter(
    private val context: Context,
    private val images: IntArray,
    private var selectedResId: Int = -1,
    private val onCharacterSelected: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = images.size
    override fun getItem(position: Int): Any = images[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, R.layout.item_character, null)
        val imageView = view.findViewById<ImageView>(R.id.characterImage)

        val drawableRes = images[position]

        Glide.with(context)
            .load(drawableRes)
            .transform(CircleCrop())
            .into(imageView)

        val backgroundRes = if (drawableRes == selectedResId)
            R.drawable.image_selected_background
        else
            R.drawable.circle_white_bg

        imageView.setBackgroundResource(backgroundRes)

        view.setOnClickListener {
            selectedResId = drawableRes
            onCharacterSelected(drawableRes)
            notifyDataSetChanged()
        }

        return view
    }
}

