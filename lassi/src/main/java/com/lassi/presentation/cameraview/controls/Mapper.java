package com.lassi.presentation.cameraview.controls;


import com.lassi.presentation.cameraview.audio.Facing;
import com.lassi.presentation.cameraview.audio.Flash;
import com.lassi.presentation.cameraview.audio.Hdr;
import com.lassi.presentation.cameraview.audio.WhiteBalance;

abstract class Mapper {

    abstract <T> T map(Flash flash);

    abstract <T> T map(Facing facing);

    abstract <T> T map(WhiteBalance whiteBalance);

    abstract <T> T map(Hdr hdr);

    abstract <T> Flash unmapFlash(T cameraConstant);

    abstract <T> Facing unmapFacing(T cameraConstant);

    abstract <T> WhiteBalance unmapWhiteBalance(T cameraConstant);

    abstract <T> Hdr unmapHdr(T cameraConstant);
}
