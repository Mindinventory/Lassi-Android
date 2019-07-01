package com.lassi.presentation.cameraview.video;

import java.nio.ByteBuffer;

class InputBuffer {
    ByteBuffer data;
    ByteBuffer source;
    int index;
    int length;
    long timestamp;
    boolean isEndOfStream;
}
