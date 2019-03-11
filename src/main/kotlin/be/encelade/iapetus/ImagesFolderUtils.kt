package be.encelade.iapetus

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

    fun File.resizeImagesTo(width: Int, height: Int, output: String = "${this.absolutePath}-${width}x$height") {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach {
                try {
                    val image = ImageIO.read(it)
                    if (image.width >= width && image.height >= height) {
                        val resized = ImageUtils.resize(image, width, height, image.type)
                        ImageIO.write(resized, "jpg", File("$output/${it.name}"))
                    } else {
                        println("image ${it.name} is smaller than $width x $height")
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
    }

    fun File.cropImagesToProportions(x: Int, y: Int, output: String = "${this.absolutePath}-${x}x$y") {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach {
                try {
                    val image = ImageIO.read(it)
                    val cropped = image.cropToProportions(x, y)
                    ImageIO.write(cropped, "jpg", File("$output/${it.name}"))
                } catch (e: Exception) {
                    println(e)
                }
            }
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
