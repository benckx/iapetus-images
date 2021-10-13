package be.encelade.iapetus.utils

import be.encelade.iapetus.ImageUtils
import org.apache.commons.lang3.RandomStringUtils
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Utils to resize and crop folders of images.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object ImagesFolderUtils {

    private val extensions = listOf("jpg", "jpeg", "png")

    /**
     * List all images in folder and sub-folders
     */
    fun File.listAllImages(): List<File> {
        validateDirectory()
        val result = mutableListOf<File>()

        // TODO: flat map?
        listFiles()
            .sorted()
            .forEach {
                if (it.isDirectory) {
                    result.addAll(it.listAllImages())
                } else if (it.isFile && extensions.contains(it.name.split(".").last())) {
                    result.add(it)
                }
            }

        return result
    }

    fun File.resizeImagesTo(width: Int, height: Int, output: String = "${this.absolutePath}-${width}x$height"): File {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach { imageFile ->
                try {
                    val image = ImageIO.read(imageFile)
                    if (image.width >= width && image.height >= height) {
                        val resized = ImageUtils.resize(image, width, height, image.type)
                        ImageIO.write(resized, imageFile.extension, File("$output/${imageFile.name}"))
                    } else {
                        println("image ${imageFile.name} is smaller than $width x $height")
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }

        return File(output)
    }

    fun File.cropImagesToSquares(output: String = "${this.absolutePath}-square"): File {
        return cropImagesToProportions(1, 1, output)
    }

    fun File.cropImagesToProportions(x: Int, y: Int, output: String = "${this.absolutePath}-${x}x$y"): File {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach { imageFile ->
                try {
                    val image = ImageIO.read(imageFile)
                    val cropped = image.cropToProportions(x, y)
                    ImageIO.write(cropped, imageFile.extension, File("$output/${imageFile.name}"))
                } catch (e: Exception) {
                    println(e)
                }
            }

        return File(output)
    }

    fun File.randomizeImageNames(output: String = "${this.absolutePath}-rnd"): File {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .forEach { imageFile ->
                val randomFileName = "${RandomStringUtils.randomAlphanumeric(24)}.${imageFile.extension}"
                imageFile.renameTo(File(randomFileName))
            }

        return File(output)
    }

    private fun File.validateDirectory() {
        if (!exists() || !isDirectory) {
            throw IllegalArgumentException("$this is not a folder or doesn't exist")
        }
    }

    private fun initDirectory(output: String) {
        println("output folder: $output")

        val outputFolder = File(output)

        if (outputFolder.exists()) {
            outputFolder.deleteRecursively()
        }

        outputFolder.mkdir()
    }

    private fun BufferedImage.cropToProportions(x: Int, y: Int): BufferedImage {
        val targetProportion = x.toDouble() / y.toDouble()
        val imageProportion = this.width.toDouble() / this.height.toDouble()
        when {
            imageProportion > targetProportion -> {
                // crop left and right
                val targetWidth = (this.height / y) * x
                val margins = (this.width - targetWidth) / 2
                return this.getSubimage(margins, 0, targetWidth, this.height)
            }
            imageProportion < targetProportion -> {
                // crop top and bottom
                val targetHeight = (this.width / x) * y
                val margins = (this.height - targetHeight) / 2
                return this.getSubimage(0, margins, this.width, targetHeight)
            }
            else -> {
                return this
            }
        }
    }

}
