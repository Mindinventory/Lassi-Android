package com.lassi.presentation.cameraview.preview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lassi.R;
import com.lassi.presentation.cameraview.utils.CameraLogger;

// Fallback preview when hardware acceleration is off.
// Currently this does NOT support cropping (e. g. the crop inside behavior),
// so we return false in supportsCropping() in order to have proper measuring.
// This means that CameraView is forced to be wrap_content.
public class SurfaceCameraPreview extends CameraPreview<SurfaceView, SurfaceHolder> {

    private final static CameraLogger LOG = CameraLogger.create(SurfaceCameraPreview.class.getSimpleName());

    private boolean mDispatched;
    private View mRootView;

    public SurfaceCameraPreview(@NonNull Context context, @NonNull ViewGroup parent, @Nullable CameraPreview.SurfaceCallback callback) {
        super(context, parent, callback);
    }

    @NonNull
    @Override
    protected SurfaceView onCreateView(@NonNull Context context, @NonNull ViewGroup parent) {
        View root = LayoutInflater.from(context).inflate(R.layout.cameraview_surface_view, parent, false);
        parent.addView(root, 0);
        SurfaceView surfaceView = root.findViewById(R.id.surface_view);
        final SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // This is too early to call anything.
                // surfaceChanged is guaranteed to be called after, with exact dimensions.
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LOG.i("callback:", "surfaceChanged", "w:", width, "h:", height, "dispatched:", mDispatched);
                if (!mDispatched) {
                    dispatchOnSurfaceAvailable(width, height);
                    mDispatched = true;
                } else {
                    dispatchOnSurfaceSizeChanged(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LOG.i("callback:", "surfaceDestroyed");
                dispatchOnSurfaceDestroyed();
                mDispatched = false;
            }
        });
        mRootView = root;
        return surfaceView;
    }

    @NonNull
    @Override
    public View getRootView() {
        return mRootView;
    }

    @NonNull
    @Override
    public SurfaceHolder getOutput() {
        return getView().getHolder();
    }

    @NonNull
    @Override
    public Class<SurfaceHolder> getOutputClass() {
        return SurfaceHolder.class;
    }
}
