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
    }

    // 일몰 애니메이션
    private fun startSunsetAnimation() {
        val sunYStart = sunView.top.toFloat()
        val sunYEnd = skyView.height.toFloat()

        val heightAnimator = ObjectAnimator.ofFloat(sunView, "y", sunYStart, sunYEnd)
            .setDuration(3000)
            .apply { interpolator = AccelerateInterpolator() }

        // 후광도 함께 이동하도록 설정
        val haloHeightAnimator = ObjectAnimator.ofFloat(sunHaloView, "y", sunYStart, sunYEnd)
            .setDuration(3000)
            .apply { interpolator = AccelerateInterpolator() }

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
        val haloHeightAnimator = ObjectAnimator.ofFloat(sunHaloView, "y", sunYStart, sunYEnd)
            .setDuration(3000)
            .apply { interpolator = AccelerateInterpolator() }

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
        // 태양이 커졌다 작아졌다 하는 애니메이션
        val scaleUpX = ObjectAnimator.ofFloat(sunView, "scaleX", 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = OvershootInterpolator()
        }

        val scaleUpY = ObjectAnimator.ofFloat(sunView, "scaleY", 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = OvershootInterpolator()
        }

        val alphaAnimator = ObjectAnimator.ofFloat(sunView, "alpha", 1f, 0.9f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        // 후광 효과 (halo) 확장 & 축소
        val haloScaleX = ObjectAnimator.ofFloat(sunHaloView, "scaleX", 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val haloScaleY = ObjectAnimator.ofFloat(sunHaloView, "scaleY", 1f, 1.3f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val haloAlpha = ObjectAnimator.ofFloat(sunHaloView, "alpha", 0.5f, 0.9f, 0.5f).apply {
            duration = 1000
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