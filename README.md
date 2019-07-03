# Lassi
[![](https://jitpack.io/v/Mindinventory/Lassi.svg)](https://jitpack.io/#Mindinventory/Lassi)

Lassi is simplest way to pick media (either image or video) 

### Lassi Image picker
![image](/media/image-picker.png) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![image](/media/image-picker-camera.gif)

### Lassi Video picker
![image](/media/video-picker.png) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![image](/media/video-picker-camera.gif)

### Key features

* Simple implementation 
* Set your own custom styles
* Filter by particular media type
* Filter videos by min and max time
* Enable/lassiOption camera from media picker

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
            implementation 'com.github.Mindinventory:Lassi:0.0.3'
        }
    ``` 

### Implementation


* Step 1. Add Lassi in to your activity class:
    
    ```kotlin
            val intent = Lassi(this)
                .with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY or CAMERA_AND_GALLERY
                .setMaxCount(5)
                .setGridSize(3)
                .setMediaType(MediaType.VIDEO) // MediaType : VIDEO or IMAGE
                .setMinTime(15) // for MediaType.VIDEO only
                .setMaxTime(30) // for MediaType.VIDEO only
                .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp") // Filter by limited media format (Optional)
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
    
### Requirements

* minSdkVersion >= 17
* Androidx

### Library used

* [Glide](https://github.com/bumptech/glide) 
* [CameraView](https://github.com/natario1/CameraView)
* [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)

### ProGaurd rules

-dontwarn com.bumptech.glide.**

# LICENSE!

MiMediaPicker is [MIT-licensed](/LICENSE).

# Let us know!
We’d be really happy if you send us links to your projects where you use our component. Just send an email to sales@mindinventory.com And do let us know if you have any questions or suggestion regarding our work.
