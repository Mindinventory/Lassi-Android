package com.lassi.presentation.cameraview.video;

class InputBufferPool extends Pool<InputBuffer> {

    InputBufferPool() {
        super(Integer.MAX_VALUE, new Pool.Factory<InputBuffer>() {
            @Override
            public InputBuffer create() {
                return new InputBuffer();
            }
        });
    }
}
