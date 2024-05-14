package pk.codebase.navigationdrawer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 201;

    private TextureView textureView;
    private Button captureButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        textureView = view.findViewById(R.id.textureView);
        captureButton = view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        return view;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            // Save the captured image data into a file
            saveImageToFile(data);
        }
    }

    private void saveImageToFile(Intent data) {
        // Get the image data from the intent
        Bundle extras = data.getExtras();
        if (extras != null) {
            // Retrieve the captured image
            Object image = extras.get("data");
            if (image instanceof android.graphics.Bitmap) {
                // Convert the image to byte array
                android.graphics.Bitmap bitmapImage = (android.graphics.Bitmap) image;
                // Create a directory named "cryptography" in internal storage
                File directory = new File(requireContext().getFilesDir(), "cryptography");
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // Generate a unique file name
                String fileName = "image_" + System.currentTimeMillis() + ".png";
                // Create a new file
                File file = new File(directory, fileName);
                // Write the image data to the file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bitmapImage.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    Toast.makeText(requireContext(), "Image saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Log.d("ImagePath", "Image saved: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    // Other methods like onResume(), requestCameraPermission(), onSurfaceTextureAvailable(), etc. remain the same.
    // Ensure you have the necessary permissions declared in the manifest file.
}
