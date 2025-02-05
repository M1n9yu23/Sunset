package com.bignerdranch.android.sunset

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: View
    private lateinit var sunView: View
    private lateinit var sunHaloView: View  // 후광 효과를 위한 뷰
    private lateinit var skyView: View

    private val blueSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.blue_sky)
    }

    private val sunsetSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.sunset_sky)
    }

    private val nightSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.night_sky)
    }

    private var isSunset = false
    private var currentAnimator: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.scene)
        sunView = findViewById(R.id.sun)
        sunHaloView = findViewById(R.id.sun_halo) // 후광 효과
        skyView = findViewById(R.id.sky)

        sceneView.setOnClickListener {
            if (currentAnimator?.isRunning == true) {
                reverseAnimation()
            } else {
                if (isSunset) {
                    startSunriseAnimation()
                } else {
                    startSunsetAnimation()
                }
            }
        }

        sunHaloView.x = sunView.x + (sunView.width - sunHaloView.width) / 2
        sunHaloView.y = sunView.y + (sunView.height - sunHaloView.height) / 2

    }

    // 일몰 애니메이션
    private fun startSunsetAnimation() {
        val sunYStart = sunView.top.toFloat()
        val sunYEnd = skyView.height.toFloat()

        val heightAnimator = ObjectAnimator.ofFloat(sunView, "y", sunYStart, sunYEnd)
            .setDuration(3000)
            .apply { interpolator = AccelerateInterpolator() }

        // 후광도 함께 이동하도록 설정
        val haloHeightAnimator = ObjectAnimator.ofFloat(
            sunHaloView, "y",
            sunHaloView.y, // 초기 위치
            sunHaloView.y + (sunYEnd - sunYStart) // 태양과 같은 거리 이동
        ).setDuration(3000).apply {
            interpolator = AccelerateInterpolator()
        }


        val sunsetSkyAnimator = ObjectAnimator.ofInt(skyView, "backgroundColor", blueSkyColor, sunsetSkyColor)
            .setDuration(3000)
            .apply { setEvaluator(ArgbEvaluator()) }

        val nightSkyAnimator = ObjectAnimator.ofInt(skyView, "backgroundColor", sunsetSkyColor, nightSkyColor)
            .setDuration(1500)
            .apply { setEvaluator(ArgbEvaluator()) }

        val sunEffectAnimator = createSunEffectAnimation()

        currentAnimator = AnimatorSet().apply {
            playTogether(heightAnimator, haloHeightAnimator, sunsetSkyAnimator, sunEffectAnimator)
            play(nightSkyAnimator).after(heightAnimator)
            start()
        }

        isSunset = true
    }

    private fun startSunriseAnimation() {
        val sunYStart = skyView.height.toFloat()
        val sunYEnd = sunView.top.toFloat()

        val heightAnimator = ObjectAnimator.ofFloat(sunView, "y", sunYStart, sunYEnd)
            .setDuration(3000)
            .apply { interpolator = AccelerateInterpolator() }

        // 후광도 함께 이동하도록 설정
        val haloHeightAnimator = ObjectAnimator.ofFloat(
            sunHaloView, "y",
            sunHaloView.y, // 초기 위치
            sunHaloView.y + (sunYEnd - sunYStart) // 태양과 같은 거리 이동
        ).setDuration(3000).apply {
            interpolator = AccelerateInterpolator()
        }


        val nightToSunsetSkyAnimator = ObjectAnimator.ofInt(skyView, "backgroundColor", nightSkyColor, sunsetSkyColor)
            .setDuration(1500)
            .apply { setEvaluator(ArgbEvaluator()) }

        val sunsetToDaySkyAnimator = ObjectAnimator.ofInt(skyView, "backgroundColor", sunsetSkyColor, blueSkyColor)
            .setDuration(3000)
            .apply { setEvaluator(ArgbEvaluator()) }

        val sunEffectAnimator = createSunEffectAnimation()

        currentAnimator = AnimatorSet().apply {
            playTogether(heightAnimator, haloHeightAnimator, nightToSunsetSkyAnimator, sunEffectAnimator)
            play(sunsetToDaySkyAnimator).after(nightToSunsetSkyAnimator)
            start()
        }

        isSunset = false
    }

    // 태양 + 후광 효과 (타오르는 느낌)
    private fun createSunEffectAnimation(): AnimatorSet {
        // 태양 크기 변화 (부드럽게)
        val scaleUpX = ObjectAnimator.ofFloat(sunView, "scaleX", 1f, 1.15f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = OvershootInterpolator()
        }

        val scaleUpY = ObjectAnimator.ofFloat(sunView, "scaleY", 1f, 1.15f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = OvershootInterpolator()
        }

        // 태양 밝기 변화 (부드러운 빛의 변화)
        val alphaAnimator = ObjectAnimator.ofFloat(sunView, "alpha", 1f, 0.95f, 1f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        // 후광 효과 (빛이 서서히 확장)
        val haloScaleX = ObjectAnimator.ofFloat(sunHaloView, "scaleX", 1f, 1.2f).apply {
            duration = 2500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val haloScaleY = ObjectAnimator.ofFloat(sunHaloView, "scaleY", 1f, 1.2f).apply {
            duration = 2500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        // 후광 밝기 변화 (부드러운 빛의 깜빡임)
        val haloAlpha = ObjectAnimator.ofFloat(sunHaloView, "alpha", 0.7f, 0.9f, 0.75f).apply {
            duration = 2500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        return AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY, alphaAnimator, haloScaleX, haloScaleY, haloAlpha)
        }
    }


    // 현재 애니메이션을 반대로 실행
    private fun reverseAnimation() {
        currentAnimator?.cancel()

        if (isSunset) {
            startSunriseAnimation()
        } else {
            startSunsetAnimation()
        }
    }
}