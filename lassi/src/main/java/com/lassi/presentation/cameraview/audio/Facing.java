package com.lassi.presentation.cameraview.audio;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lassi.presentation.cameraview.controls.CameraView;
import com.lassi.presentation.cameraview.utils.CameraUtils;

/**
 * Facing value indicates which camera sensor should be used for the current session.
 *
 * @see CameraView#setFacing(Facing)
 */
public enum Facing implements Control {

    /**
     * Back-facing camera sensor.
     */
    BACK(0),

    /**
     * Front-facing camera sensor.
     */
    FRONT(1);

    private int value;

    Facing(int value) {
        this.value = value;
    }

    @NonNull
    public static Facing DEFAULT(@Nullable Context context) {
        if (context == null) {
            return BACK;
        } else if (CameraUtils.hasCameraFacing(context, BACK)) {
            return BACK;
        } else if (CameraUtils.hasCameraFacing(context, FRONT)) {
            return FRONT;
        } else {
            // The controller will throw a CameraException.
            // This device has no cameras.
            return BACK;
        }
    }

    @Nullable
    public static Facing fromValue(int value) {
        Facing[] list = Facing.values();
        for (Facing action : list) {
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
