package com.lassi.presentation.cameraview.audio;


import androidx.annotation.Nullable;

import com.lassi.presentation.cameraview.controls.CameraOptions;
import com.lassi.presentation.cameraview.controls.CameraView;

/**
 * White balance values control the white balance settings.
 *
 * @see CameraView#setWhiteBalance(WhiteBalance)
 */
public enum WhiteBalance implements Control {

    /**
     * Automatic white balance selection (AWB).
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedWhiteBalance()
     */
    AUTO(0),

    /**
     * White balance appropriate for incandescent light.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedWhiteBalance()
     */
    INCANDESCENT(1),

    /**
     * White balance appropriate for fluorescent light.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedWhiteBalance()
     */
    FLUORESCENT(2),

    /**
     * White balance appropriate for daylight captures.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedWhiteBalance()
     */
    DAYLIGHT(3),

    /**
     * White balance appropriate for pictures in cloudy conditions.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedWhiteBalance()
     */
    CLOUDY(4);

    public static final WhiteBalance DEFAULT = AUTO;

    private int value;

    WhiteBalance(int value) {
        this.value = value;
    }

    @Nullable
    public static WhiteBalance fromValue(int value) {
        WhiteBalance[] list = WhiteBalance.values();
        for (WhiteBalance action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return null;
    }

    public int value() {
        return value;
    }
}