package com.fmaldonado.chip8.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.TableRow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fmaldonado.chip8.data.emu.Chip8Data
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

        setUpKeys()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpKeys() {
        var row = TableRow(this)
        for (i in Chip8Data.KEY_NAMES.indices) {
            val button = Button(this)
            button.text = Chip8Data.KEY_NAMES[i]
            button.tag = Chip8Data.KEY_VALUES[i]
            button.setOnTouchListener(buttonTouchListener)
            row.addView(button)


            if ((i + 1) % 4 == 0) {
                binding.keyboard.addView(row)
                row = TableRow(this)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val buttonTouchListener = OnTouchListener { v: View, event: MotionEvent ->
        val key = v.tag as Int
        if (event.action == MotionEvent.ACTION_DOWN) {
            viewModel.addToKeyBuffer(key)
        } else if (event.action == MotionEvent.ACTION_UP) {
            viewModel.removeKeyFromBuffer(key)
        }
        false
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