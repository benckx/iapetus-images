package be.encelade.iapetus

import java.awt.BasicStroke
import java.awt.Color.*
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.imageio.ImageIO.read as readImage

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FaceUtils {

    val FACE_CLOSE_UP = ImageContainingFace(640, 1080, Face(Rectangle(64, 270, 512, 540)))
    val WAIST_LEVEL = ImageContainingFace(640, 1080, Face(Rectangle(170, 170, 300, 300)))
    val KNEE_LEVEL = ImageContainingFace(640, 1080, Face(Rectangle(245, 50, 150, 150)))

    fun reFrameAroundFace(csv: String, frame: ImageContainingFace, output: String, debug: Boolean = false) {
        val outputFolder = File(output)

        if (outputFolder.exists()) {
            outputFolder.deleteRecursively()
        }

        outputFolder.mkdir()

        parseFacesCSV(csv)
            .entries
            .parallelStream()
            .forEach {
                val inputImage = ImageIO.read(File(it.key))

                val debugImage = BufferedImage(inputImage.width, inputImage.height, TYPE_INT_RGB)
                val graphics2D = debugImage.createGraphics()
                if (debug) {
                    graphics2D.drawRenderedImage(inputImage, AffineTransform())
                    graphics2D.stroke = BasicStroke(5f)
                }

                var i = 0
                it.value.forEach { face ->
                    val inputImageWithFace = ImageContainingFace(inputImage, face)

                    if (debug) {
                        graphics2D.color = BLUE
                        graphics2D.draw(face.rectangle)

                        graphics2D.color = RED
                        val outerRectangle = inputImageWithFace.getFrameOutsideRectangle(frame)
                        graphics2D.draw(outerRectangle)

                        graphics2D.color = ORANGE
                        val innerRectangle = inputImageWithFace.getFrameInnerRectangle(frame)
                        graphics2D.draw(innerRectangle)
                    }

                    if (inputImageWithFace.canFitInto(frame)) {
                        val resizedFrame = inputImageWithFace.resize(frame)
                        try {
                            val subImage = inputImageWithFace.getSubImage(inputImage, resizedFrame)
                            if (subImage.width >= resizedFrame.width && subImage.height >= resizedFrame.width) {
                                val cropped = ImageUtils.resize(subImage, resizedFrame.width, resizedFrame.height)
                                val outputImage = File("$output/framed_${i}_${it.key.split("/").last()}")
                                ImageIO.write(cropped, "jpg", outputImage)
                            }
                        } catch (e : Exception) {
                            println(e)
                            println(face)
                        }
                    }
                    i++
                }

                if (debug) {
                    ImageIO.write(debugImage, "jpg", File("$output/debug_${it.key.split("/").last()}"))
                }
            }
    }

    fun parseFacesCSV(csv: String): Map<String, List<Face>> {
        val result = mutableMapOf<String, List<Face>>()

        Files
            .readAllLines(Paths.get(csv))
            .forEach {
                try {
                    val split = it.split(",")
                    val fileName = split[0]
                    val score = split[1].toDouble()
                    val x1 = split[2].toInt()
                    val y1 = split[3].toInt()
                    val x2 = split[4].toInt()
                    val y2 = split[5].toInt()
                    val rectangle = Rectangle(x1, y1, x2 - x1, y2 - y1)

                    val face = Face(fileName, rectangle, score)

                    if (!result.containsKey(fileName)) {
                        result[fileName] = mutableListOf(face)
                    } else {
                        val faces = result[fileName]
                        (faces as MutableList).add(face)
                    }
                } catch (e: Exception) {
                    println("error: $e")
                }
            }

        return result
    }
}
