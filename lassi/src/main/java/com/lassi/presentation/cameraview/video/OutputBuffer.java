package com.lassi.presentation.cameraview.video;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

class OutputBuffer {
    MediaCodec.BufferInfo info;
    int trackIndex;
    ByteBuffer data;
}
