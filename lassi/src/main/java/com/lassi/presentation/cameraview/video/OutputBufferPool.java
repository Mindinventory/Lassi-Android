package com.lassi.presentation.cameraview.video;

import android.media.MediaCodec;

class OutputBufferPool extends Pool<OutputBuffer> {

    OutputBufferPool(final int trackIndex) {
        super(Integer.MAX_VALUE, new Pool.Factory<OutputBuffer>() {
            @Override
            public OutputBuffer create() {
                OutputBuffer buffer = new OutputBuffer();
                buffer.trackIndex = trackIndex;
                buffer.info = new MediaCodec.BufferInfo();
                return buffer;
            }
        });
    }
}
