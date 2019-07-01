package com.lassi.presentation.cameraview.video;

import java.nio.ByteBuffer;

class ByteBufferPool extends Pool<ByteBuffer> {

    ByteBufferPool(final int bufferSize, int maxPoolSize) {
        super(maxPoolSize, new Pool.Factory<ByteBuffer>() {
            @Override
            public ByteBuffer create() {
                return ByteBuffer.allocateDirect(bufferSize);
            }
        });
    }
}
