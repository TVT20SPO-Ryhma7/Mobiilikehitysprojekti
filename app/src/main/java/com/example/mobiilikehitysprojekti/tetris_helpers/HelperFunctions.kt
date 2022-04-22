package com.example.mobiilikehitysprojekti.tetris_helpers

fun array2dOfByte(sizeOuter: Int, sizeInner: Int): Array<ByteArray>
        = Array(sizeOuter) { ByteArray(sizeInner) }

//This code generates the byte array used to make the block shapes.
//First argument is the desired row number of the array
//Second argument is the desired collumn number