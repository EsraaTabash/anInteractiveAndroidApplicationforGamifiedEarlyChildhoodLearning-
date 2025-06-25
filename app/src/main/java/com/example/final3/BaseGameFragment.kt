package com.example.final3

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
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
    private lateinit var loadingOverlaylayout: FrameLayout
    private lateinit var loadingRocket: LottieAnimationView

    protected abstract fun getCardPrefix(): String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlaylayout = view.findViewById(R.id.loadingOverlaylayout)
        loadingRocket = view.findViewById(R.id.loadingRocket)

        student = view.findViewById(R.id.std1)
        student.bringToFront()
        initializeCards(view)

        loadProgress()
    }

    private fun loadProgress() {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

        val lastUnlockedCardSP = prefs.getInt("last_unlocked_card", -1)
        val lastCompletedCardSP = prefs.getInt("last_completed_card", -1)

        if (userType == "under6") {
            // الطفل الصغير
            if (lastUnlockedCardSP != -1 && lastCompletedCardSP != -1) {
                loadProgressFromPrefsAndSetupUI(prefs)
            } else {
                // لا توجد بيانات - نبدأ من الصفر
                prefs.edit()
                    .putInt("last_unlocked_card", 0)
                    .putInt("last_completed_card", -1)
                    .apply()
                loadProgressFromPrefsAndSetupUI(prefs)
            }
        } else {
            // الطفل الكبير
            if (lastUnlockedCardSP != -1 && lastCompletedCardSP != -1) {
                loadProgressFromPrefsAndSetupUI(prefs)
            } else {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    showLoading()
                    FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val gameId = doc.getLong("gameId")?.toInt() ?: 0
                            val lastCompletedCard = doc.getLong("last_completed_card")?.toInt() ?: -1

                            prefs.edit()
                                .putInt("last_unlocked_card", gameId)
                                .putInt("last_completed_card", lastCompletedCard)
                                .apply()

                            hideLoading()
                            loadProgressFromPrefsAndSetupUI(prefs)
                        }
                        .addOnFailureListener {
                            hideLoading()
                            loadProgressFromPrefsAndSetupUI(prefs)
                        }
                } else {
                    loadProgressFromPrefsAndSetupUI(prefs)
                }
            }
        }
    }

    private fun loadProgressFromPrefsAndSetupUI(prefs: SharedPreferences) {
        val lastUnlockedCard = prefs.getInt("last_unlocked_card", 0)
        val lastCompletedCard = prefs.getInt("last_completed_card", -1)

        Log.d("BaseGameFragment", "loadProgressFromPrefsAndSetupUI -> lastUnlockedCard: $lastUnlockedCard, lastCompletedCard: $lastCompletedCard")

        currentCardIndex = maxOf(lastCompletedCard + 1, lastUnlockedCard)
        Log.d("BaseGameFragment", "Set currentCardIndex to: $currentCardIndex")

        for (i in cards.indices) {
            when {
                i <= lastCompletedCard -> {
                    cards[i].visibility = View.VISIBLE
                    cards[i].alpha = 0.3f
                    cards[i].bringToFront()
                    cards[i].isClickable = true
                }
                i == currentCardIndex -> {
                    cards[i].visibility = View.VISIBLE
                    cards[i].alpha = 1f
                    cards[i].isClickable = true
                }
                else -> {
                    cards[i].visibility = View.INVISIBLE
                    cards[i].isClickable = false
                }
            }
        }

        setupCardListeners()

        student.post {
            Log.d("BaseGameFragment", "Moving student to card index $currentCardIndex")
            moveStudentToCard(currentCardIndex)
        }
    }

    private fun setupCardListeners() {
        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                lastPlayedCardIndex = index
                startGame(index + 1)
            }
        }
    }

    protected fun onGameEnd(index: Int) {
        if (index == -1) return

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        if (index >= currentCardIndex) {
            val lastUnlockedCard = index + 1
            val lastCompletedCard = index

            prefs.edit()
                .putInt("last_unlocked_card", lastUnlockedCard)
                .putInt("last_completed_card", lastCompletedCard)
                .apply()

            score += 100
            cards[index].alpha = 0.3f

            if (index + 1 < cards.size) {
                currentCardIndex = index + 1
                moveStudentToCard(index + 1)
                cards[index + 1].visibility = View.VISIBLE
                cards[index + 1].alpha = 1f
            } else {
                showToast("أكملت جميع الكواكب! 🎉")
            }

            val userType = prefs.getString("user_type", "under6") ?: "under6"
            if (userType != "under6") {
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    val dataToUpdate = hashMapOf<String, Any>(
                        "gameId" to lastUnlockedCard,
                        "last_completed_card" to lastCompletedCard
                    )
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update(dataToUpdate)
                }
            }
        } else {
            cards[index].alpha = 0.3f
        }
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

    private fun showLoading() {
        loadingOverlaylayout.visibility = View.VISIBLE
        loadingRocket.playAnimation()
    }

    private fun hideLoading() {
        loadingRocket.cancelAnimation()
        loadingOverlaylayout.visibility = View.GONE
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

    private val studentOffsets = listOf(
        Pair(0f, 0f), Pair(-120f, -100f), Pair(-170f, 50f),
        Pair(-40f, 80f), Pair(-200f, 20f), Pair(150f, -200f)
    )

    private fun moveStudentToCard(index: Int) {
        if (index < 0 || index >= cards.size) return

        val nextCard = cards[index]
        val (offsetX, offsetY) = studentOffsets.getOrNull(index) ?: Pair(0f, 0f)

        val animX = ObjectAnimator.ofFloat(student, "x", nextCard.x + offsetX)
        val animY = ObjectAnimator.ofFloat(student, "y", nextCard.y + (nextCard.height - student.height) / 2f + offsetY)

        animX.duration = 1000
        animY.duration = 1000
        animX.start()
        animY.start()
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
                        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val previousScore = prefs.getInt("game_score", 0)
                        val updatedScore = previousScore + scoreFromUnity

                        prefs.edit().putInt("game_score", updatedScore).apply()

                        score += scoreFromUnity

                        showToast("نتيجتك في اللعبة: $scoreFromUnity 🎮")

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
