package com.example.analogclocktestvk

import android.content.res.Configuration
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.analogclocktestvk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var clockView: ClockView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clockView = ClockView(this, circleColor = getColor(R.color.black)).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0
            ).apply {
                weight = 1f
            }
        }
        binding.root.addView(clockView)
        binding.root.orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                LinearLayout.VERTICAL
            } else LinearLayout.HORIZONTAL
    }
}