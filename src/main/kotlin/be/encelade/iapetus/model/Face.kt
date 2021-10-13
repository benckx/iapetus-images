package be.encelade.iapetus.model

import java.awt.Rectangle

data class Face(val filePath: String, val rectangle: Rectangle, val score: Double) {

    constructor(rectangle: Rectangle) : this("", rectangle, 0.toDouble())
}
