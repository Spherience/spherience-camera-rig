package com.spherience.camerarigandroid;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    //private MediaRecorder mMediaRecorder;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";

    CameraSurfaceView surface_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (SurfaceView) findViewById(R.id.surface_view);

        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setKeepScreenOn(true);
        surface_view = new CameraSurfaceView(this);
        //mGLView = new MySurfaceView(this);
        //setContentView(mGLView);
        addContentView(surface_view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }


    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link android.media.MediaRecorder} and {@link android.hardware.Camera}. When not recording,
     * it prepares the {@link android.media.MediaRecorder} and starts recording.
     *
     * @param view the view generating the event.
     */
    public void onCaptureClick(View view) {
        if (isRecording) {

            // stop recording and release camera
            //mMediaRecorder.stop();  // stop the recording
            //releaseMediaRecorder(); // release the MediaRecorder object
            //mCamera.lock();         // take camera access back from MediaRecorder

            isRecording = false;
            releaseCamera();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        //releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

//    private void releaseMediaRecorder(){
////        if (mMediaRecorder != null) {
////            // clear recorder configuration
////            mMediaRecorder.reset();
////            // release the recorder object
////            mMediaRecorder.release();
////            mMediaRecorder = null;
////            // Lock camera for later use i.e taking it back from MediaRecorder.
////            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
//         //   mCamera.lock();
//        if (mWs.isOpen())
//            mWs.sendClose();
//        //}
//    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder(){

        Log.d("!!!!!!!!!!!!", "prepare!!");
        releaseCamera();
        Log.d("!!!!!!!!!!!!", "released camera");
        mCamera = CameraHelper.getDefaultCameraInstance();

        Log.d("!!!!!!!!!!!!", "got new instance");
        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        //profile.fileFormat = MediaRecorder.OutputFormat.THREE_GPP;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        //parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(parameters);
        return true;
    }

    public void previewCamera()
    {
        try
        {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e)
        {
            Log.d(TAG, "Cannot start preview.", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null)
        {
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        if (mCamera != null)
        {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size previewSize = getPreviewSize();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            previewCamera();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (null != mCamera) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("booooo", "Error setting camera preview: " + e.getMessage());
            }
        }
    }

        public Camera.Size getPreviewSize()
    {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        return CameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                mPreview.getWidth(), mPreview.getHeight());
    }
}
