package com.example.final3
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Above6AgeFragment : BaseGameFragment() {

    override fun getCardPrefix(): String = "Above6"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_above_6_age, container, false)
    }
}
