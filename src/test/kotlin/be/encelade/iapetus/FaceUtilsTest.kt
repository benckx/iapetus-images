package be.encelade.iapetus

import be.encelade.iapetus.utils.FaceUtils.WAIST_LEVEL
import be.encelade.iapetus.utils.FaceUtils.reFrameAroundFace
import org.junit.Test
import java.io.File

class FaceUtilsTest {

    @Test
    fun test1() {
        val outputFolder = "test_output_faces"
        val csv = File("src/test/resources/beatles.csv").absolutePath
        reFrameAroundFace(csv, WAIST_LEVEL, outputFolder, debug = true)
    }

}
