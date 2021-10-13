<a href="https://paypal.me/benckx/2">
<img src="https://img.shields.io/badge/Donate-PayPal-green.svg"/>
</a>

# About

Collection of functions to prepare datasets for Machine Learning training;

## ImagesUtils

Various functions to resize, compress, rotate, etc. individual images (in Java).

## ImagesFolderUtils

Methods that apply operation on an input folder (and its sub-folders recursively) containing images (in Kotlin). It
doesn't override the original images, but create an output folder with the processed images.

### List all images in folders and sub-folders:

```kotlin
File.listAllImages()
```

### Crop all images found in input folder to proportions (e.g. 1x1, 16x9, etc.):

```kotlin
File.cropImagesToProportions(x = 1, y = 2)
```

### Resize all images found in input folder:

```kotlin
File.resizeImagesTo(width = 1920, height = 1080)
File.resizeImagesTo(size = 1024)
```

All those functions return a `File`, and therefore can be chained:

```kotlin
val folder = File("/home/user/Pictures/dataset/my_data_set")

folder
    .cropImagesToSquares()
    .resizeImagesTo(1024)
    .randomizeImageNames()
```

```
drwxrwxr-x  2 benoit benoit 3,8M Oct 13 18:05 my_data_set
drwxrwxr-x  2 benoit benoit 3,9M Oct 13 18:12 my_data_set-square
drwxrwxr-x  2 benoit benoit 3,8M Oct 13 18:18 my_data_set-square-1024
drwxrwxr-x  2 benoit benoit 2,2M Oct 13 18:18 my_data_set-square-1024-rnd
```

## FaceUtils

Crop pictures around the faces (in such a way that faces are always at the same place in the output images), based on
information contained in a CSV file. You can build such a CSV with this
project: https://github.com/benckx/tensorflow-face-detection

    FaceUtils.reFrameAroundFace(csv: String, frame: ImageContainingFace, output: String)

For example, with this input image and frame (frames outlines added for visualization purpose; the CSV face data is in
blue, the WAIST_LEVEL frame in red and orange):

    val WAIST_LEVEL = ImageContainingFace(640, 1080, Face(Rectangle(170, 170, 300, 300)))

![](assets/debug_Beatles_with_Ed_Sullivan.jpg)

It will extract the following:

![](assets/framed_0_Beatles_with_Ed_Sullivan.jpg)
![](assets/framed_1_Beatles_with_Ed_Sullivan.jpg)
![](assets/framed_2_Beatles_with_Ed_Sullivan.jpg)
![](assets/framed_3_Beatles_with_Ed_Sullivan.jpg)
![](assets/framed_4_Beatles_with_Ed_Sullivan.jpg)

# Import with Gradle

    repositories {
        maven { url "https://jitpack.io" }
    }
    
    dependencies {
        compile "com.github.benckx:iapetus-images:master-SNAPSHOT"
    }
