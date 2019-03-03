package be.encelade.iapetus

import be.encelade.iapetus.FaceUtils.WAIST_LEVEL
import be.encelade.iapetus.FaceUtils.reFrameAroundFace
import be.encelade.iapetus.ImagesFolderUtils.resizeImagesTo
import org.junit.Test
import java.io.File

class FaceUtilsTest {

    @Test
    fun test1() {
        val outputFolder = "test_output_faces"
        val csv = File("src/test/resources/beatles.csv").absolutePath
        reFrameAroundFace(csv, WAIST_LEVEL, outputFolder, debug = true)

        File("test_output_faces").resizeImagesTo(160, 270)
    }
}
