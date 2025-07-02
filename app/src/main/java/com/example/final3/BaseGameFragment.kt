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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        // 1. حمل السكور المحلي أولاً حتى لو لم يوجد بيانات في الشيرد بريفيرنس
        loadScore()

        // 2. حمل آخر تقدم للبطاقات
        loadProgress()
    }

    // تحميل السكور فقط من SharedPreferences وعرضه على الـ UI بدون كراش
    private fun loadScore() {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val scoreLocal = prefs.getInt("game_score", 0)
        score = scoreLocal
        updateScoreUI(scoreLocal)
    }

    // تعديل: إزالة أي استعمال لحقل last_completed_card نهائيا!
    private fun loadProgress() {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

        val lastUnlockedCardSP = prefs.getInt("last_unlocked_card", 0)

        if (userType == "under6") {
            // الطفل الصغير: يبدأ من الصفر إذا لم يكن هناك تقدم
            prefs.edit()
                .putInt("last_unlocked_card", lastUnlockedCardSP)
                .apply()
            loadProgressFromPrefsAndSetupUI(prefs)
        } else {
            // الطفل الكبير: حمّل آخر تقدم من Firestore (أو محلياً إذا ما فيه نت)
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                showLoading()
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val gameId = doc.getLong("gameId")?.toInt() ?: 0
                        val scoreFS = doc.getLong("score")?.toInt() ?: 0

                        prefs.edit()
                            .putInt("last_unlocked_card", gameId)
                            .putInt("game_score", scoreFS)
                            .apply()

                        score = scoreFS
                        hideLoading()
                        loadProgressFromPrefsAndSetupUI(prefs)
                        updateScoreUI(scoreFS)
                    }
                    .addOnFailureListener {
                        hideLoading()
                        loadProgressFromPrefsAndSetupUI(prefs)
                        updateScoreUI(score) // يعرض آخر سكورتعرف عليه محليا (او صفر)
                    }
            } else {
                loadProgressFromPrefsAndSetupUI(prefs)
                updateScoreUI(score)
            }
        }
    }

    private fun loadProgressFromPrefsAndSetupUI(prefs: SharedPreferences) {
        val lastUnlockedCard = prefs.getInt("last_unlocked_card", 0)
        currentCardIndex = lastUnlockedCard

        for (i in cards.indices) {
            when {
                i < lastUnlockedCard -> {
                    cards[i].visibility = View.VISIBLE
                    cards[i].alpha = 0.3f
                    cards[i].bringToFront()
                    cards[i].isClickable = true
                }
                i == lastUnlockedCard -> {
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

    // عند إنهاء اللعبة - التحديث فقط على last_unlocked_card
    protected fun onGameEnd(index: Int) {
        if (index == -1) return

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (index >= currentCardIndex) {
            val lastUnlockedCard = index + 1
            prefs.edit()
                .putInt("last_unlocked_card", lastUnlockedCard)
                .apply()

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
                        "gameId" to lastUnlockedCard
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

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

        // الكل يذهب لنفس الـ Activity
        val intent = Intent(requireContext(), com.unity3d.player.UnityPlayerActivity::class.java)
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
        Pair(-100f, -100f), Pair(-120f, -100f), Pair(-170f, 50f),
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
            val scoreFromUnity = data?.getIntExtra("score", -1) ?: -1

            if (scoreFromUnity != -1) {
                val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val previousScore = prefs.getInt("game_score", 0)
                val updatedScore = previousScore + scoreFromUnity
                val userType = prefs.getString("user_type", "under6") ?: "under6"

                // احسب آخر تقدم حقيقي للطفل
                val lastUnlockedCard = lastPlayedCardIndex + 1
                if (lastUnlockedCard > prefs.getInt("last_unlocked_card", 0)) {
                    prefs.edit().putInt("last_unlocked_card", lastUnlockedCard).apply()
                }

                prefs.edit().putInt("game_score", updatedScore).apply()
                score = updatedScore

                showToast("نتيجتك في اللعبة: $scoreFromUnity 🎮")
                updateScoreUI(updatedScore)

                // حدث Firestore دفعة واحدة لو الطفل كبير
                if (userType != "under6") {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val dataToUpdate = mapOf(
                            "score" to updatedScore,
                            "gameId" to lastUnlockedCard
                        )
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update(dataToUpdate)
                            .addOnSuccessListener {
                                Log.d("BaseGameFragment", "تم تحديث السكور والتقدم معًا على Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("BaseGameFragment", "فشل تحديث البيانات على Firestore", e)
                            }
                    }
                }

                onGameEnd(lastPlayedCardIndex)
            } else {
                showToast("⚠️ لم يتم استقبال النتيجة من Unity")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // دالة آمنة لتحديث الـ UI (تحمي من NullPointerException)
    private fun updateScoreUI(newScore: Int) {
        val tvScore = try {
            requireView().findViewById<TextView>(R.id.tv_score)
        } catch (e: Exception) {
            null
        }
        tvScore?.text = "عدد النجــوم التي حصلت عليــها : $newScore"
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
        updateScoreUI(score)
    }
}
