package com.fmaldonado.chip8.ui.activities.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmaldonado.chip8.data.emu.Chip8Emulator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject
constructor(
    private val chip8: Chip8Emulator
) : ViewModel() {

    val screen = MutableLiveData(chip8.display.toList())
    val keyBuffer = IntArray(16)
    fun loadFile(input: InputStream) {
        try {
            chip8.restart()
            chip8.loadProgram(input)
            run()
        } catch (e: Exception) {
            Log.e("ViewModel error", "Error", e)
        }
    }

    private fun run() {
        viewModelScope.launch {
            while (true) {
                chip8.setKeyBuffer(keyBuffer)
                chip8.runProgram()
                if (chip8.needsRedraw) {
                    val newList = mutableListOf<Byte>()
                    newList.addAll(chip8.display.toList())
                    screen.postValue(newList)
                    chip8.needsRedraw = false
                }
                delay(8)
            }
        }
    }

    fun addToKeyBuffer(key: Int) {
        keyBuffer[key] = 1
    }

    fun removeKeyFromBuffer(key: Int) {
        keyBuffer[key] = 0
    }

}