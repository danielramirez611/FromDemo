package com.gbs.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // Establece el layout de splash screen

        // Configura un retraso de 4 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            // Inicia la siguiente actividad
            val intent = Intent(this, CasoActivity::class.java) // Reemplaza 'NextActivity' con la actividad que deseas abrir
            startActivity(intent)
            finish() // Finaliza la actividad actual para que el usuario no pueda volver a ella
        }, 4000) // 4000 milisegundos = 4 segundos
    }
}
