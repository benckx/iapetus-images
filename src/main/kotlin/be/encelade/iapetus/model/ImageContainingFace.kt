package be.encelade.iapetus.model

import java.awt.Rectangle
import java.awt.image.BufferedImage

data class ImageContainingFace(val width: Int, val height: Int, val face: Face) {

    constructor(image: BufferedImage, face: Face) : this(image.width, image.height, face)

    fun canFitInto(targetFrame: ImageContainingFace): Boolean {
        val targetFrameOuterRectangle = getFrameOutsideRectangle(targetFrame)
        return targetFrameOuterRectangle.x > 0
                && targetFrameOuterRectangle.y > 0
                && targetFrameOuterRectangle.x + targetFrameOuterRectangle.width < width
                && targetFrameOuterRectangle.y + targetFrameOuterRectangle.height < height
    }

    fun getSubImage(image: BufferedImage, targetFrame: ImageContainingFace): BufferedImage {
        val targetFrameOuterRectangle = getFrameOutsideRectangle(targetFrame)
        return image.getSubimage(
            targetFrameOuterRectangle.x,
            targetFrameOuterRectangle.y,
            targetFrameOuterRectangle.width,
            targetFrameOuterRectangle.height
        )
    }

    fun getFrameOutsideRectangle(targetFrame: ImageContainingFace): Rectangle {
        val resizedFrame = targetFrame.resize(getRatio(targetFrame))
        val x = centerOffsetX() - resizedFrame.centerOffsetX()
        val y = centerOffsetY() - resizedFrame.centerOffsetY()
        return Rectangle(x, y, resizedFrame.width, resizedFrame.height)
    }

    fun getFrameInnerRectangle(targetFrame: ImageContainingFace): Rectangle {
        val resizedFrame = targetFrame.resize(getRatio(targetFrame))
        val x = centerOffsetX() - resizedFrame.centerOffsetX()
        val y = centerOffsetY() - resizedFrame.centerOffsetY()
        return Rectangle(
            x + resizedFrame.face.rectangle.x,
            y + resizedFrame.face.rectangle.y,
            resizedFrame.face.rectangle.width,
            resizedFrame.face.rectangle.height
        )
    }

    fun resize(targetFrame: ImageContainingFace) : ImageContainingFace {
        return targetFrame.resize(getRatio(targetFrame))
    }

    private fun getRatio(targetFrame: ImageContainingFace): Double {
        return face.rectangle.height.toDouble() / targetFrame.face.rectangle.height.toDouble()
    }

    private fun resize(ratio: Double): ImageContainingFace {
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        val newX = (face.rectangle.x * ratio).toInt()
        val newY = (face.rectangle.y * ratio).toInt()
        val newFaceWidth = (face.rectangle.width * ratio).toInt()
        val newFaceHeight = (face.rectangle.height * ratio).toInt()

        val newFace = Face(face.filePath, Rectangle(newX, newY, newFaceWidth, newFaceHeight), face.score)

        return ImageContainingFace(newWidth, newHeight, newFace)
    }

    private fun centerOffsetX(): Int {
        return face.rectangle.x + (face.rectangle.width / 2)
    }

    private fun centerOffsetY(): Int {
        return face.rectangle.y + (face.rectangle.height / 2)
    }
}
