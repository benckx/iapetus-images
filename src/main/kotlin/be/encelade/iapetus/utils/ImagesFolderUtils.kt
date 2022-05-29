package be.encelade.iapetus.utils

import be.encelade.iapetus.ImageUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.apache.commons.lang3.RandomUtils.nextInt
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.util.concurrent.Executors
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
        listFiles()!!
            .sorted()
            .forEach { file ->
                if (file.isDirectory) {
                    result.addAll(file.listAllImages())
                } else if (file.isFile && extensions.contains(file.name.split(".").last())) {
                    result.add(file)
                }
            }

        return result
    }

    fun File.resizeImagesTo(size: Int, outputFolder: String = "${this.absolutePath}-$size"): File {
        return resizeImagesTo(size, size, outputFolder)
    }

    fun File.resizeImagesTo(
        width: Int,
        height: Int,
        outputFolder: String = "${this.absolutePath}-${width}x$height"
    ): File {
        validateDirectory()
        initDirectory(outputFolder)

        listAllImages()
            .parallelStream()
            .forEach { imageFile ->
                try {
                    val image = ImageIO.read(imageFile)
                    if (image.width >= width && image.height >= height) {
                        val resized = ImageUtils.resize(image, width, height, image.type)
                        ImageIO.write(resized, imageFile.extension, File("$outputFolder/${imageFile.name}"))
                    } else {
                        println("image ${imageFile.name} is smaller than $width x $height")
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }

        return File(outputFolder)
    }

    fun File.cropImagesToSquares(outputFolder: String = "${this.absolutePath}-square"): File {
        return cropImagesToProportions(1, 1, outputFolder)
    }

    fun File.cropImagesToProportions(x: Int, y: Int, outputFolder: String = "${this.absolutePath}-${x}x$y"): File {
        validateDirectory()
        initDirectory(outputFolder)

        listAllImages()
            .parallelStream()
            .forEach { imageFile ->
                try {
                    val image = ImageIO.read(imageFile)
                    val cropped = image.cropToProportions(x, y)
                    ImageIO.write(cropped, imageFile.extension, File("$outputFolder${File.separator}${imageFile.name}"))
                } catch (e: Exception) {
                    println(e)
                }
            }

        return File(outputFolder)
    }

    fun File.randomizeImageNames(outputFolder: String = "${this.absolutePath}-rnd"): File {
        validateDirectory()
        initDirectory(outputFolder)

        listAllImages()
            .forEach { imageFile ->
                val randomFileName = "${randomAlphanumeric(24)}.${imageFile.extension}"
                FileUtils.copyFile(imageFile, File("$outputFolder${File.separator}$randomFileName"))
            }

        return File(outputFolder)
    }

    // TODO: add shift in output folder name
    /**
     * Assumes all input images have the same resolution.
     */
    fun File.createMosaics(
        width: Int,
        height: Int,
        nbrOfOutputImages: Int,
        maxShiftX: Int = 0,
        maxShiftY: Int = 0,
        outputFolder: String = "${absolutePath}-mosaic-${width}x$height"
    ): File {
        validateDirectory()
        initDirectory(outputFolder)

        val imageFiles = listAllImages()
        val nbrOfInputImages = imageFiles.size
        val executor = Executors.newFixedThreadPool(20)

        repeat(nbrOfOutputImages) {
            executor.execute {
                val baseImageFile = imageFiles[nextInt(0, nbrOfInputImages)]
                val baseImage = ImageIO.read(baseImageFile)
                val baseImageX = (width - baseImage.width) / 2 + (nextInt(0, maxShiftX * 2) - maxShiftX)
                val baseImageY = (height - baseImage.height) / 2 + (nextInt(0, maxShiftY * 2) - maxShiftY)
                val outputImage = BufferedImage(width, height, TYPE_INT_RGB)
                val graphics2D = outputImage.createGraphics()

                val outputRectangle = Rectangle(0, 0, width, height) // boundaries
                val drawnRectangles = mutableListOf<Rectangle>() // avoid drawing twice the same location

                fun drawRandomImageAt(x: Int, y: Int) {
                    val imageFile = imageFiles[nextInt(0, nbrOfInputImages)]
                    val image = ImageIO.read(imageFile)
                    graphics2D.drawImage(image, null, x, y)
                    drawnRectangles += Rectangle(x, y, image.width, image.height)

                    // recursion
                    rectanglesAround(image, x, y)
                        .filter { rectangle -> rectangle.intersects(outputRectangle) }
                        .filterNot { rectangle -> drawnRectangles.contains(rectangle) }
                        .forEach { rectangle ->
                            drawRandomImageAt(rectangle.x, rectangle.y)
                        }
                }

                drawRandomImageAt(baseImageX, baseImageY)
                val randomFileName = "${randomAlphanumeric(24).uppercase()}.png"
                ImageIO.write(outputImage, "png", File("$outputFolder${File.separator}$randomFileName"))
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(50)
        }

        return File(outputFolder)
    }

    fun File.cutVerticalStripes(nbrOfStripes: Int, output: String = "${this.absolutePath}-v$nbrOfStripes") {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach {
                try {
                    val image = ImageIO.read(it)
                    val stripeWidth = image.width / nbrOfStripes
                    val fileName = it.name.split(".").first()
                    val extension = it.name.split(".").last()

                    (0 until nbrOfStripes).forEach { i ->
                        val stripe = image.getSubimage(stripeWidth * i, 0, stripeWidth, image.height)
                        ImageIO.write(stripe, "jpg", File("$output/$fileName-strip$i.$extension"))
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
    }

    fun File.cutHorizontalStripes(nbrOfStripes: Int, output: String = "${this.absolutePath}-h$nbrOfStripes") {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach {
                try {
                    val image = ImageIO.read(it)
                    val stripeHeight = image.height / nbrOfStripes
                    val fileName = it.name.split(".").first()
                    val extension = it.name.split(".").last()

                    (0 until nbrOfStripes).forEach { i ->
                        val stripe = image.getSubimage(0, stripeHeight * i, image.width, stripeHeight)
                        ImageIO.write(stripe, "jpg", File("$output/$fileName-strip$i.$extension"))
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
    }

    fun File.cutPiecesOf(width: Int, height: Int, output: String = "${this.absolutePath}-${width}x${height}") {
        validateDirectory()
        initDirectory(output)

        listAllImages()
            .parallelStream()
            .forEach {
                try {
                    val image = ImageIO.read(it)
                    val xCuts = image.width / width
                    val yCuts = image.height / height
                    val fileName = it.name.split(".").first()
                    val extension = it.name.split(".").last()

                    (0 until xCuts).forEach { x ->
                        (0 until yCuts).forEach { y ->
                            val stripe = image.getSubimage(x * width, y * height, width, height)
                            ImageIO.write(stripe, extension, File("$output/$fileName-${x}x${y}.$extension"))
                        }
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
    }

    /**
     * @return The 8 [[Rectangle]] locations around that [[BufferedImage]] (+ the image rectangle itself)
     */
    private fun rectanglesAround(image: BufferedImage, imageX: Int, imageY: Int): List<Rectangle> {
        return IntRange(-1, 1).flatMap { x ->
            IntRange(-1, 1).map { y ->
                Rectangle(
                    imageX + x * image.width,
                    imageY + y * image.height,
                    image.width,
                    image.height
                )
            }
        }
    }

    private fun File.validateDirectory() {
        if (!exists() || !isDirectory) {
            throw IllegalArgumentException("$this is not a folder or doesn't exist")
        }
    }

    private fun initDirectory(outputFolderName: String) {
        println("output folder: $outputFolderName")

        val outputFolder = File(outputFolderName)

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
