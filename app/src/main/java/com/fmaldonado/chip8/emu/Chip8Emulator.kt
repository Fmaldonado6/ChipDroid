package com.fmaldonado.chip8.emu

import java.io.DataInputStream
import kotlin.experimental.and
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
    var needsRedraw = false

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
        needsRedraw = false
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
            0x7000 -> {
                val x = (opcodeCode and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                V[x] = ((V[x] + nn).code and 0xFF).toChar()
                programCounter += 2
            }
            0x8000 -> {
                when (opcodeCode and 0x000F) {
                    0x0000 -> {
                        val x = (opcodeCode and 0x0F00) shr 8
                        val y = (opcodeCode and 0x00F0) shr 4
                        V[x] = V[y]
                        programCounter += 2
                    }
                    0x0001 -> {
                        val x = (opcodeCode and 0x0F00) shr 8
                        val y = (opcodeCode and 0x00F0) shr 4
                        V[x] = (V[x].code or V[y].code).toChar()
                        programCounter += 2
                    }
                    0x0002 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val y = (opcode.code and 0x00F0) shr 4
                        V[x] = (V[x].code and V[y].code).toChar()
                        programCounter += 2
                    }
                    0x0003 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val y = (opcode.code and 0x00F0) shr 4
                        V[x] = (V[x].code xor V[y].code).toChar()
                        programCounter += 2
                    }
                    0x0004 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val y = (opcode.code and 0x00F0) shr 4
                        V[0xF] = (if (V[y].code > 0xFF - V[x].code) 1 else 0).toChar()
                        V[x] = ((V[x].code + V[y].code) and 0xFF).toChar()
                        programCounter += 2
                    }
                    0x0005 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val y = (opcode.code and 0x00F0) shr 4
                        V[0xF] = (if (V[x].code > V[y].code) 1 else 0).toChar()
                        V[x] = ((V[x].code - V[y].code) and 0xFF).toChar()
                        programCounter += 2
                    }
                    0x0006 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        V[0xF] = (V[x].code and 0x1).toChar()
                        V[x] = (V[x].code shr 1).toChar()
                        programCounter += 2
                    }
                    0x0007 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val y = (opcode.code and 0x00F0) shr 4
                        V[0xF] = (if (V[x].code > V[y].code) 0 else 1).toChar()
                        V[x] = ((V[y].code - V[x].code) and 0xFF).toChar()
                        programCounter += 2
                    }
                    0x000E -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        V[0xF] = (V[x].code and 0x80).toChar()
                        V[x] = (V[x].code shl 1).toChar()
                        programCounter += 2
                    }
                    else -> {
                        throw Error("Unsupported Opcode!")
                    }
                }
            }
            0x9000 -> {
                val x = (opcode.code and 0x0F00) shr 8
                val y = (opcode.code and 0x00F0) shr 4
                programCounter += if (V[x].code != V[y].code) 4 else 2
            }
            0xA000 -> {
                index = (opcodeCode and 0x0FFF).toChar()
                programCounter += 2
            }
            0xB000 -> {
                val nnn = opcodeCode and 0x0FFF
                val extra = V[0].code and 0xFF
                programCounter = (nnn + extra).toChar()
            }
            0xC000 -> {
                val x = (opcode.code and 0x0F00) shr 8
                val nn = opcodeCode and 0x00FF
                val random = (0..256).random() and nn
                V[x] = random.toChar()
                programCounter += 2
            }
            0xD000 -> {
                val x = (opcode.code and 0x0F00) shr 8
                val y = (opcode.code and 0x00F0) shr 4
                val height = opcodeCode and 0x000F

                V[0xF] = 0.toChar()

                for (_y in 0..height) {
                    val line = memory[index.code + _y]
                    for (_x in 0..8) {
                        val pixel = line.code and (0x80 shr _x)
                        if (pixel == 0)
                            continue

                        var totalX = x + _x
                        var totalY = y + _y

                        totalX %= 64;
                        totalY %= 32;

                        val displayIndex = totalY * 64 + totalX

                        if (display[displayIndex].code == 1)
                            V[0xF] = 1.toChar()

                        display[displayIndex] = (display[displayIndex].code xor 1).toChar()
                    }

                    programCounter += 2
                    needsRedraw = true
                }
            }
            0xE000 -> {
                when (opcodeCode and 0x00FF) {
                    0x009E -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val currentKey = V[x]
                        programCounter += if (key[currentKey.code].code == 1) 4 else 2
                    }
                    0x00A1 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val currentKey = V[x]
                        programCounter += if (key[currentKey.code].code == 0) 4 else 2
                    }
                    else -> {
                        throw Error("Unsupported opcode")
                    }
                }
            }
            0xF000 -> {
                when (opcodeCode and 0x00FF) {
                    0x0007 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        V[x] = delayTime
                        programCounter += 2
                    }
                    0x000A -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        for (i in key.indices) {
                            if (key[i].code != 1)
                                continue
                            V[x] = i.toChar()
                            programCounter += 2
                        }
                    }
                    0x0015 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        delayTime = V[x]
                        programCounter += 2
                    }
                    0x0018 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        soundTimer = V[x]
                        programCounter += 2
                    }
                    0x001E -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        index += V[x].code
                        programCounter += 2
                    }
                    0x0029 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        val character = V[x]
                        index = (0x050 + (character.code * 5)).toChar()
                        programCounter += 2
                    }
                    0x0033 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        var value = V[x].code
                        val hundreds = (value - (value % 100)) / 100
                        value -= hundreds * 100
                        val tens = (value - (value % 10)) / 10
                        value -= tens * 10
                        memory[index.code] = hundreds.toChar()
                        memory[index.code + 1] = tens.toChar()
                        memory[index.code + 2] = value.toChar()
                        programCounter += 2
                    }
                    0x0055 -> {
                        val x = (opcode.code and 0x0F00) shr 8
                        for (i in 0..x) {
                            V[i] = memory[index.code + i]
                        }
                        index += x + 1
                        programCounter += 2
                    }
                    else -> {
                        throw Error("Unsupported opcode")
                    }
                }
            }
            else -> {
                throw Error("Unsupported opcode")
            }
        }

        if (delayTime.code > 0)
            delayTime--

        if (soundTimer.code > 0)
            soundTimer--
    }

    fun loadProgram(input: DataInputStream) {
        var offset = 0
        while (input.available() > 0) {
            memory[0x200 + offset] = ((input.readByte()).toInt() and 0xFF).toChar()
            offset++
        }
        input.close()
    }

    fun loadFontset() {
        for (i in Chip8Data.FONTSET.indices)
            memory[0x50 + i] = (Chip8Data.FONTSET[i] and 0xFF).toChar()
    }

    fun setKeyBuffer(keyBuffer: IntArray) {
        for (i in key.indices)
            key[i] = keyBuffer[i].toChar()
    }


}