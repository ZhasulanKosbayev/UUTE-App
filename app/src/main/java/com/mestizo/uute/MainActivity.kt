package com.mestizo.uute

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "УУТЭ Калькулятор v1.0\nПриложение успешно скомпилировано!"
        textView.textSize = 20f
        textView.setPadding(40, 40, 40, 40)
        
        setContentView(textView)
    }
}
