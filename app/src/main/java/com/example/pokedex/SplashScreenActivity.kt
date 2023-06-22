package com.example.pokedex

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import pl.droidsonroids.gif.GifDrawable

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var gifImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        gifImageView = findViewById(R.id.gifImageView)
        val gifDrawable = GifDrawable(resources, R.drawable.loading)
        gifImageView.setImageDrawable(gifDrawable)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_DURATION)
    }

    companion object {
        private const val SPLASH_DURATION = 2000L
    }
}