package com.maiwavo.arcore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArSceneView arSceneView;
    private Session session;
    private boolean configureSession = false;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arSceneView = (ArSceneView)findViewById(R.id.arView);
/*
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }


 */
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        initSceneView();
    }

    private void setupSession() {
        if (session == null) {
            try {
                session = new Session(this);
            }
            catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            }
            catch (UnavailableDeviceNotCompatibleException e) {
                e.printStackTrace();
            }
            catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            }
            catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            }
            configureSession = true;
        }

        if (configureSession) {
            configureSession();
            configureSession = false;
            arSceneView.setupSession(session);
        }

        try {
            session.resume();
            arSceneView.resume();
        }
        catch (CameraNotAvailableException e) {
            e.printStackTrace();
            session = null;
        }
    }

    private void configureSession() {
        Config config = new Config(session);
        if (!buildDatabase(config)) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
    }

    private boolean buildDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap = loadImage();
        if (bitmap == null) {
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("mouse", bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadImage() {
        try {
            InputStream inputStream = getAssets().open("mouse.png");
            return BitmapFactory.decodeStream(inputStream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initSceneView() {
        arSceneView.getScene().addOnUpdateListener(this);
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arSceneView.getArFrame();
        try {
            Collection<AugmentedImage> augmentedImageCollection = frame.getUpdatedTrackables(AugmentedImage.class);

            for (AugmentedImage image : augmentedImageCollection) {
                if (image.getTrackingState() == TrackingState.TRACKING) {
                    if (image.getName().equals("mouse")) {
                        ARNode arNode = new ARNode(this, R.raw.sheep);
                        arNode.setImage(image);
                        arSceneView.getScene().addChild(arNode);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        /*
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

         */
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (session != null) {
            arSceneView.pause();
            session.pause();
        }
    }
}