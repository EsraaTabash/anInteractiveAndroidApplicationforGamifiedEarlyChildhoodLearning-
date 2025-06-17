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
    private var selectedId: Int = 1,
    private val onCharacterSelected: (Int) -> Unit
) : BaseAdapter() {

    private val characterIds = intArrayOf(1, 2, 3, 4, 5, 6)

    override fun getCount(): Int = characterIds.size
    override fun getItem(position: Int): Any = characterIds[position]
    override fun getItemId(position: Int): Long = characterIds[position].toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, R.layout.item_character, null)
        val imageView = view.findViewById<ImageView>(R.id.characterImage)

        val characterId = characterIds[position]
        val drawableRes = CharacterUtils.getDrawableByCharacterId(characterId)

        imageView.setImageResource(drawableRes) // الأفضل والأضمن مع الصور المحلية

        val backgroundRes = if (characterId == selectedId)
            R.drawable.image_selected_background
        else
            R.drawable.circle_white_bg

        imageView.setBackgroundResource(backgroundRes)

        view.setOnClickListener {
            selectedId = characterId
            onCharacterSelected(characterId)
            notifyDataSetChanged()
        }

        return view
    }
}
