package com.cufa.workshop

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = TextView(this).apply {
            text = "CUFA WORKSHOP\n\nTerminet • Automjetet • AI Diagnostika • OBD Live"
            textSize = 22f
            setPadding(48,96,48,48)
        }
        setContentView(v)
    }
}
