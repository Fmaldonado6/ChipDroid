package com.fmaldonado.chip8.ui.activities.main

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fmaldonado.chip8.R
import com.fmaldonado.chip8.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission()

        binding.emulatorScreen.post {

            val scale = binding.emulatorScreen.width / 64F

            binding.emulatorScreen.scale = scale
        }


        val result = registerForActivityResult(ActivityResultContracts.GetContent()) { it: Uri? ->
            if (it == null)
                return@registerForActivityResult
            val input = contentResolver.openInputStream(it) ?: return@registerForActivityResult
            viewModel.loadFile(input)
            input.close()
        }

        viewModel.screen.observe(this, {
            Log.d("REDRAW", "YES")
            binding.emulatorScreen.display = it
            binding.emulatorScreen.invalidate()
        })

        binding.selectFile.setOnClickListener {
            result.launch("application/*")
        }
    }


    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }
    }


}