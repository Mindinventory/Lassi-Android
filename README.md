# Lassi
[![](https://jitpack.io/v/Mindinventory/Lassi.svg)](https://jitpack.io/#Mindinventory/Lassi)

Lassi is simplest way to pick media (either image, video, audio or doc) 

### Lassi Media picker
![image](/media/image-picker.png) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![image](/media/image-picker-camera.gif)

### Key features

* Simple implementation 
* Set your own custom styles
* Filter by particular media type
* Filter videos by min and max time
* Enable/disable camera from LassiOption

# Usage

### Dependencies

* Step 1. Add the JitPack repository to your build file
    
    Add it in your root build.gradle at the end of repositories:

    ```groovy
	    allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
    ``` 

* Step 2. Add the dependency
    
    Add it in your app module build.gradle:
    
    ```groovy
        dependencies {
            ...
            implementation 'com.github.Mindinventory:Lassi:0.1.7'
        }
    ``` 

### Implementation


* Step 1. Add Lassi in to your activity class:
    
    ```kotlin
            val intent = Lassi(this)
                .with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                .setMaxCount(5)
                .setGridSize(3)
                .setMediaType(MediaType.VIDEO) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                .setCompressionRation(10) // compress image for single item selection (can be 0 to 100)
                .setMinTime(15) // for MediaType.VIDEO only
                .setMaxTime(30) // for MediaType.VIDEO only
                .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp") // Filter by limited media format (Optional)
                .setMinFileSize(100) // Restrict by minimum file size 
                .setMaxFileSize(1024) //  Restrict by maximum file size
                .disableCrop() // to remove crop from the single image selection (crop is enabled by default for single image)
                /*
                 * Configuration for  UI
                 */
                .setStatusBarColor(R.color.colorPrimaryDark)
                .setToolbarResourceColor(R.color.colorPrimary)
                .setProgressBarColor(R.color.colorAccent)
                .setPlaceHolder(R.drawable.ic_image_placeholder)
                .setErrorDrawable(R.drawable.ic_image_placeholder)
                .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                .setCropAspectRatio(1, 1) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                .enableActualCircleCrop() // Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
                .build()
            startActivityForResult(intent, MEDIA_REQUEST_CODE)
    ```


* Step 2. override onActivityResult function to get Lassi result.

    ```kotlin
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && data != null) {
                when (requestCode) {
                    MEDIA_REQUEST_CODE -> {
                        val selectedMedia = data.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                        // Do needful with your selectedMedia
                    }
                }
            }
        }
    ```


### Guideline for contributors
Contribution towards our repository is always welcome, we request contributors to create a pull request to the **develop** branch only.  

### Guideline to report an issue/feature request
It would be great for us if the reporter can share the below things to understand the root cause of the issue.

* Library version
* Code snippet
* Logs if applicable
* Device specification like (Manufacturer, OS version, etc)
* Screenshot/video with steps to reproduce the issue

### Requirements

* minSdkVersion >= 17
* Androidx

### Library used

* [Glide](https://github.com/bumptech/glide)
* [CameraView](https://github.com/natario1/CameraView)
* [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)
* [Bridge](https://github.com/livefront/bridge)

### ProGaurd rules

-dontwarn com.bumptech.glide.**

# LICENSE!

Lassi is [MIT-licensed](/LICENSE).

# Let us know!
We’d be really happy if you send us links to your projects where you use our component. Just send an email to sales@mindinventory.com And do let us know if you have any questions or suggestion regarding our work.
