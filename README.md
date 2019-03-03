# About

Some ImageUtils and ImagesFolderUtils methods implemented in Java and Kotlin.

## ImagesUtils

Methods to resize, compress, rotate, etc. individual images (in Java).

## ImagesFolderUtils

Methods that apply operation on an input folder (and its sub-folders recursively) containing images (in Kotlin).
It doesn't override the original images, but create an output folder with the processed images.

- List all images in folders and sub-folders:

      listAllImages()
    
- Crop all images from input folder to proportions (e.g. 1x1, 16x9, etc.):    
    
      cropImagesToProportions(x: Int, y: Int)

- Resize all images found in folder to:

      resizeImagesTo(width: Int, height: Int)

# Import with Gradle

    repositories {
        maven { url "https://jitpack.io" }
    }
    
    dependencies {
        compile "com.github.benckx:iapetus-images:master-SNAPSHOT"
    }
