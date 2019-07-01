package com.lassi.presentation.cameraview.controls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface for video recording.
 * Don't call start if already started. Don't call stop if already stopped.
 * Don't reuse.
 */
abstract class VideoRecorder {

    protected Exception mError;
    /* tests */ VideoResult mResult;
    /* tests */ VideoResultListener mListener;

    VideoRecorder(@NonNull VideoResult stub, @Nullable VideoResultListener listener) {
        mResult = stub;
        mListener = listener;
    }

    abstract void start();

    abstract void stop();

    @SuppressWarnings("WeakerAccess")
    protected void dispatchResult() {
        if (mListener != null) {
            mListener.onVideoResult(mResult, mError);
            mListener = null;
            mResult = null;
            mError = null;
        }
    }


    interface VideoResultListener {
        void onVideoResult(@Nullable VideoResult result, @Nullable Exception exception);
    }
}
