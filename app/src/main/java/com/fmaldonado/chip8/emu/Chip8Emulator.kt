package com.fmaldonado.chip8.emu

import kotlin.properties.Delegates

class Chip8Emulator {

    companion object {
        const val MEMORY_SIZE = 4096
        const val CPU_REGISTER_SIZE = 16
        const val STACK_SIZE = 16
        const val DISPLAY_SIZE = 64 * 32
        const val KEY_COUNT = 16
    }

    private lateinit var memory: CharArray
    private lateinit var V: CharArray
    private var index by Delegates.notNull<Char>()
    private var programCounter by Delegates.notNull<Char>()

    private var opcode by Delegates.notNull<Char>()
    private lateinit var display: CharArray

    private lateinit var stack: CharArray
    private var stackPointer by Delegates.notNull<Short>()

    private var delayTime by Delegates.notNull<Char>()
    private var soundTimer by Delegates.notNull<Char>()

    private lateinit var key: CharArray

    init {
        restart()
    }

    fun restart() {
        memory = CharArray(MEMORY_SIZE)
        V = CharArray(CPU_REGISTER_SIZE)
        stack = CharArray(STACK_SIZE)
        display = CharArray(DISPLAY_SIZE)

        index = 0x0.toChar()
        programCounter = 0x0.toChar()
        stackPointer = 0

        key = CharArray(KEY_COUNT)

        delayTime = 0.toChar()
        soundTimer = 0.toChar()

        opcode = 0x0.toChar()

    }

    fun runProgram() {
        opcode =
            ((memory[programCounter.code].code shl 8) or memory[programCounter.code + 1].code).toChar()

        val opcodeCode = opcode.code

        when (opcodeCode and 0xF000) {
            0x0000 -> {
                when (opcodeCode and 0x00FF) {
                    0x00E0 -> {
                        for (i in display.indices) {
                            display[i] = 0.toChar()
                        }
                        programCounter += 2
                    }
                    0x00EE -> {
                        stackPointer--
                        programCounter = stack[stackPointer.toInt()] + 2
                    }
                }
            }
            0x1000 -> {
                val nnn = opcodeCode and 0x0FFF
                programCounter = nnn.toChar()
            }
            0x2000 -> {
                stack[stackPointer.toInt()] = programCounter
                stackPointer++
                val nnn = opcodeCode and 0x0FFF
                programCounter = nnn.toChar()
            }
            0x3000 -> {
                val x = (opcodeCode and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                programCounter += if (V[x].code == nn) 4 else 2
            }
            0x4000 -> {
                val x = (opcodeCode and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                programCounter += if (V[x].code != nn) 4 else 2
            }
            0x5000 -> {
                val y = (opcodeCode and 0x00F0) shr 4
                val x = (opcodeCode and 0x0F00) shr 8
                programCounter += if (V[x] == V[y]) 4 else 2
            }
            0x6000 -> {
                val x = (opcodeCode and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                V[x] = nn.toChar()
                programCounter += 2
            }
            0x7000 ->{
                val x = (opcodeCode and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                V[x] = ((V[x] + nn).code and 0xFF).toChar()
            }
            0xA000 -> {
                index = (opcodeCode and 0x0FFF).toChar()
                programCounter += 2
            }

        }

        if (delayTime.code > 0)
            delayTime--

        if (soundTimer.code > 0)
            soundTimer--
    }

}