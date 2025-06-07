package com.example.final3

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
//import com.unity3d.player.UnityPlayerGameActivity
import java.util.concurrent.Executors

abstract class BaseGameFragment : Fragment() {

    protected lateinit var cards: List<ImageView>
    protected lateinit var student: ImageView
    protected var currentCardIndex = 0
    private val UNITY_REQUEST_CODE = 1
    private val executor = Executors.newSingleThreadExecutor()
    private var score = 0
    protected var lastPlayedCardIndex = -1

    protected abstract fun getCardPrefix(): String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        student = view.findViewById(R.id.std1)
        student.bringToFront()
        initializeCards(view)
        setupCardListeners()
    }

    private fun initializeCards(view: View) {
        cards = listOf(
            view.findViewById(R.id.card1),
            view.findViewById(R.id.card2),
            view.findViewById(R.id.card3),
            view.findViewById(R.id.card4),
            view.findViewById(R.id.card5),
            view.findViewById(R.id.card6)
        )
    }

    private fun setupCardListeners() {
        for ((index, card) in cards.withIndex()) {
            card.setOnClickListener {
                if (index == currentCardIndex) {
                    val cardNumber = index + 1
                    Toast.makeText(requireContext(), "بدأت لعبة كوكب $cardNumber (${getCardPrefix()})", Toast.LENGTH_SHORT).show()

//                    val intent = Intent(requireContext(), UnityPlayerGameActivity::class.java)
//                    intent.putExtra("unityData", "${getCardPrefix()} card number is: $cardNumber")

                    lastPlayedCardIndex = index
                 //   startActivityForResult(intent, UNITY_REQUEST_CODE)
                }
            }
        }
    }

    protected fun onGameEnd(index: Int) {
        score += 100
        cards[index].visibility = View.INVISIBLE

        if (index + 1 < cards.size) {
            currentCardIndex = index + 1
            moveStudentToCard(currentCardIndex)
        } else {
            Toast.makeText(requireContext(), "أكملت جميع الكواكب! 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private val studentOffsets = listOf(
        Pair(0f, 0f),
        Pair(-120f, -100f),
        Pair(-170f, 50f),
        Pair(-40f, 80f),
        Pair(-200f, 20f),
        Pair(150f, -200f)
    )

    private fun moveStudentToCard(index: Int) {
        val nextCard = cards[index]
        val (offsetX, offsetY) = studentOffsets[index]

        val targetX = nextCard.x + offsetX
        val targetY = nextCard.y + (nextCard.height - student.height) / 2f + offsetY

        val animX = ObjectAnimator.ofFloat(student, "x", targetX)
        val animY = ObjectAnimator.ofFloat(student, "y", targetY)

        animX.duration = 1000
        animY.duration = 1000

        animX.start()
        animY.start()

        animY.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                nextCard.visibility = View.VISIBLE
                showToast("فتحنا كوكب ${index + 1} 🚀")
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UNITY_REQUEST_CODE && resultCode == RESULT_OK) {
            executor.submit {
                val scoreFromUnity = data?.getIntExtra("score", -1)
                requireActivity().runOnUiThread {
                    if (scoreFromUnity != -1) {
                        score += scoreFromUnity ?: 0
                        showToast("تم استقبال البيانات من Unity")
                        onGameEnd(lastPlayedCardIndex)
                    } else {
                        showToast("لم يتم استقبال البيانات بشكل صحيح من Unity")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
