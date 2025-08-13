<a href="https://www.mindinventory.com/?utm_source=gthb&utm_medium=repo&utm_campaign=lassi"><img src="https://github.com/Sammindinventory/MindInventory/blob/main/Banner.png"></a>

# Lassi [![](https://jitpack.io/v/Mindinventory/Lassi.svg)](https://jitpack.io/#Mindinventory/Lassi)



Lassi is simplest way to pick media (either image, video, audio or doc) 

### Lassi Media picker
![image](/media/image-picker.png) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![image](/media/image-picker-camera.gif)

### Key features

* Android 15 support
* Simple implementation 
* Set your own custom styles
* Filter by particular media type
* Filter videos by min and max time
* Enable/disable camera from LassiOption
* You can open System Default view for file selection by using MediaType.FILE_TYPE_WITH_SYSTEM_VIEW
* Photo Picker feature integration
* Group picked album images with camera capture for editing

# Usage

### Dependencies

* Step 1. Add the JitPack repository in your project build.gradle:

    ```groovy
	    allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
    ``` 

    **or**
    
    If Android studio version is Arctic Fox then add it in your settings.gradle:

    ```groovy
	   dependencyResolutionManagement {
    		repositories {
        		...
        		maven { url 'https://jitpack.io' }
    		}
	   }
    ``` 
    
* Step 2. Add the dependency in your app module build.gradle:
    
    ```groovy
        dependencies {
            ...
            implementation 'com.github.Mindinventory:Lassi:X.X.X'
        }
    ``` 

### Implementation


* Step 1. 
  To open an app color theme view then add Lassi in to your activity class:
    
    ```kotlin
            val intent = Lassi(this)
                .with(LassiOption.CAMERA_AND_GALLERY) // choose Option CAMERA, GALLERY, CAMERA_AND_GALLERY or PICKER
                .setMaxCount(5)
                .setGridSize(3)
                .setMediaType(MediaType.VIDEO) // MediaType : VIDEO IMAGE, AUDIO OR DOC
                .setCompressionRatio(10) // compress image for single item selection (can be 0 to 100)
                .setMinTime(15) // for MediaType.VIDEO only
                .setMaxTime(30) // for MediaType.VIDEO only
                .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp") // Filter by limited media format (Optional)
                .setMinFileSize(100) // Restrict by minimum file size 
                .setMaxFileSize(1024) // Restrict by maximum file size
                .disableCrop() // to remove crop from the single image selection (crop is enabled by default for single image)
                /*
                 * Configuration for  UI
                 */
                .setStatusBarColor(R.color.colorPrimaryDark)
                .setToolbarResourceColor(R.color.colorPrimary)
                .setProgressBarColor(R.color.colorAccent)
                .setSortingCheckedRadioButtonColor(R.color.darkBlue)    // To set color of the checked state radio button resource within sorting dialog
                .setSortingUncheckedRadioButtonColor(R.color.regentStBlue)  // To set color of the unchecked state radio button resource within sorting dialog
                .setSortingCheckedTextColor(R.color.regentStBlue)   // To set color of the checked state radio button resource within sorting dialog
                .setSortingUncheckedTextColor(R.color.darkBlue) // To set color of the unchecked state radio button resource within sorting dialog
                .setPlaceHolder(R.drawable.ic_image_placeholder)
                .setErrorDrawable(R.drawable.ic_image_placeholder)
                .setSelectionDrawable(R.drawable.ic_checked_media)
                .setAlertDialogNegativeButtonColor(R.color.cherry_red)
                .setAlertDialogPositiveButtonColor(R.color.emerald_green)
                .setGalleryBackgroundColor(R.color.colorGrey)//Customize background color of gallery (default color is white)
                .setCropType(CropImageView.CropShape.RECTANGLE) // choose shape for cropping after capturing an image from camera (for MediaType.IMAGE only)
                .setCropAspectRatio(1, 1) // define crop aspect ratio for cropping after capturing an image from camera (for MediaType.IMAGE only)
                .enableFlip() // Enable flip image option while image cropping (for MediaType.IMAGE only)
                .enableRotate() // Enable rotate image option while image cropping (for MediaType.IMAGE only)
                .enableActualCircleCrop() // Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
                .build()
             receiveData.launch(intent)
    ```
`OR` To open a system default view then add Lassi in to your activity class:

```kotlin
            val intent = Lassi(this)
                .setMediaType(MediaType.FILE_TYPE_WITH_SYSTEM_VIEW)
                .setSupportedFileTypes(
                "jpg", "jpeg", "png", "webp", "gif", "mp4", "mkv", "webm", "avi", "flv", "3gp",
                "pdf", "odt", "doc", "docs", "docx", "txt", "ppt", "pptx", "rtf", "xlsx", "xls"
                )  // Filter by required media format (Mandatory)
                .build()
            receiveData.launch(intent)
```

* Step 2. Get Lassi result in ActivityResultCallback lambda function.

    ```kotlin
        private val receiveData =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        val selectedMedia =
                            it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                        if (!selectedMedia.isNullOrEmpty()) {
                            ivEmpty.isVisible = selectedMedia.isEmpty()
                            selectedMediaAdapter.setList(selectedMedia)
                        }
                    }
                }
    ```
  
* Option - 1. To set language's text programmatically based on the current language set on the device follow below mentioned approach of setting strings in your desired language. 
* getMultiLngBuilder()  exposes params which you can use to set texts.
* By default english (en) language is set so no need to follow this step.
    ```kotlin
        val currentLang = Locale.getDefault().language.toString()
        if (currentLang == "es") {
            lassi.getMultiLngBuilder(
                setOkLbl = "d'accord",
                setCancelLbl = "Annuler",
                setSortAscendingLbl = "Ascendant",
                setSortDescendingLbl = "Descendant",
                setSortByDateLbl = "Trier par date"
            )
        }
    ```
* Option - 2. To localize text content of Lassi picker with multiple language options, define language-specific string resource file in your project and update values of string resource keys mentioned in below link with your desired language.
  * [Lassi String Resources](https://github.com/Mindinventory/Lassi-Android/blob/931e147ebe6282bd1629858b5a9f29fe5a0b8b32/lassi/src/main/res/values/strings.xml)

### Way of utilizing Photo Picker
```kotlin
        val intent = Lassi(this)
                .with(LassiOption.CAMERA_AND_GALLERY)
                .setMediaType(MediaType.PHOTO_PICKER)
                .setMaxCount(4)
                .setStatusBarColor(R.color.colorPrimaryDark)
                .setToolbarColor(R.color.colorPrimary)
                .setToolbarResourceColor(android.R.color.white)
                .setProgressBarColor(R.color.colorAccent)
                .setGalleryBackgroundColor(R.color.colorGrey)
                .setCustomLimitExceedingErrorMessage("Selected item exceeded the limit!")
                .build()
```

### Document access permission note
If Android device SDK is >= 30 and wants to access document (only for choose the non media file) then add ```android.permission.MANAGE_EXTERNAL_STORAGE``` permission in your app otherwise library won't allow to access documents. Kindly check sample app for more detail.
If you don't want to give Manage External Storage permission and wants to get files with system default view then You can use `OR` option from Step 1 and give required file type of document.

### MediaType.FILE_TYPE_WITH_SYSTEM_VIEW (for System Default View)
Using this MediaType you can choose multiple files from system default view. You can't set max count limit for file choose. Give file type into setSupportedFileTypes and you can choose only those types of file from system view.

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

* minSdkVersion >= 21
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

<a href="https://www.mindinventory.com/contact-us.php?utm_source=gthb&utm_medium=repo&utm_campaign=lassi">
<img src="https://github.com/Sammindinventory/MindInventory/blob/main/hirebutton.png" width="203" height="43"  alt="app development">
</a>
