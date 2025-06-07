package com.example.final3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Under6AgeFragment : BaseGameFragment() {

    override fun getCardPrefix(): String = "Under6"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_under_6_age, container, false)
    }
}
