package com.example.final3

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unity3d.player.UnityPlayerActivity
import java.util.concurrent.Executors

abstract class BaseGameFragment : Fragment() {

    protected lateinit var cards: List<ImageView>
    protected lateinit var student: ImageView
    protected var currentCardIndex = 0
    protected var lastPlayedCardIndex = -1
    private val UNITY_REQUEST_CODE = 1
    private val executor = Executors.newSingleThreadExecutor()
    private var score = 0

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
        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                if (index == currentCardIndex) {
                    startGame(index + 1)
                    lastPlayedCardIndex = index
                }
            }
        }
    }

    private fun startGame(gameId: Int) {
        updateGameIdToCloud(gameId)

        val intent = Intent(requireContext(), UnityPlayerActivity::class.java)
        intent.putExtra("unityData", "${getCardPrefix()} card number is: $gameId")
        startActivityForResult(intent, UNITY_REQUEST_CODE)
    }

    private fun updateGameIdToCloud(gameId: Int) {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

        if (userType == "under6") {
            prefs.edit().putInt("game_id", gameId).apply()
        } else {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid).update("gameId", gameId)
            }
        }
    }

    protected fun onGameEnd(index: Int) {
        score += 100
        cards[index].visibility = View.INVISIBLE

        if (index + 1 < cards.size) {
            currentCardIndex = index + 1
            moveStudentToCard(index + 1)
        } else {
            showToast("أكملت جميع الكواكب! 🎉")
        }
    }

    private val studentOffsets = listOf(
        Pair(0f, 0f), Pair(-120f, -100f), Pair(-170f, 50f),
        Pair(-40f, 80f), Pair(-200f, 20f), Pair(150f, -200f)
    )

    private fun moveStudentToCard(index: Int) {
        val nextCard = cards[index]
        val (offsetX, offsetY) = studentOffsets[index]

        val animX = ObjectAnimator.ofFloat(student, "x", nextCard.x + offsetX)
        val animY = ObjectAnimator.ofFloat(student, "y", nextCard.y + (nextCard.height - student.height) / 2f + offsetY)

        animX.duration = 1000
        animY.duration = 1000
        animX.start()
        animY.start()

        animY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                nextCard.visibility = View.VISIBLE
                showToast("فتحنا كوكب ${index + 1} 🚀")
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UNITY_REQUEST_CODE && resultCode == RESULT_OK) {
            executor.submit {
                val scoreFromUnity = data?.getIntExtra("score", -1) ?: -1

                requireActivity().runOnUiThread {
                    if (scoreFromUnity != -1) {
                        showToast("نتيجتك في اللعبة: $scoreFromUnity 🎮")

                        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val userType = prefs.getString("user_type", "under6") ?: "under6"

                        val previousScore = prefs.getInt("game_score", 0)
                        val updatedScore = previousScore + scoreFromUnity

                        // 🟢 حفظ المجموع الجديد
                        prefs.edit().putInt("game_score", updatedScore).apply()

                        // 🔵 تحديث Firestore فقط للمستخدم فوق 6 سنوات
                        if (userType != "under6") {
                            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(uid)
                                    .update("score", updatedScore)
                            }
                        }

                        score += scoreFromUnity
                        onGameEnd(lastPlayedCardIndex)
                    } else {
                        showToast("⚠️ لم يتم استقبال النتيجة من Unity")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
    }
}
