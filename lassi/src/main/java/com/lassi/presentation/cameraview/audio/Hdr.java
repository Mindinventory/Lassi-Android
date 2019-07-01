package com.lassi.presentation.cameraview.audio;

import androidx.annotation.Nullable;

import com.lassi.presentation.cameraview.controls.CameraView;


/**
 * Hdr values indicate whether to use high dynamic range techniques when capturing pictures.
 *
 * @see CameraView#setHdr(Hdr)
 */
public enum Hdr implements Control {

    /**
     * No HDR.
     */
    OFF(0),

    /**
     * Using HDR.
     */
    ON(1);

    public final static Hdr DEFAULT = OFF;

    private int value;

    Hdr(int value) {
        this.value = value;
    }

    @Nullable
    public static Hdr fromValue(int value) {
        Hdr[] list = Hdr.values();
        for (Hdr action : list) {
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
