package pk.codebase.navigationdrawer.fragment;

import static android.app.Activity.RESULT_OK;

import static pk.codebase.navigationdrawer.util.Helpers.bytesToHex;
import static pk.codebase.navigationdrawer.util.Helpers.hexToBytes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import io.xconn.cryptology.SealedBox;
import pk.codebase.navigationdrawer.R;
import pk.codebase.navigationdrawer.util.App;

public class CameraFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 201;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_IMAGE_PICK = 202;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_capture).setOnClickListener(v -> dispatchTakePictureIntent());
        view.findViewById(R.id.button_select_photo).setOnClickListener(v -> openGallery());
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            handleCameraResult(data);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            handleGalleryResult(data);
        }
    }

    private void handleCameraResult(@Nullable Intent data) {
        assert data != null;
        Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
        assert bitmap != null;
        byte[] imageData = bitmapToByteArray(bitmap);

        byte[] publicKey = hexToBytes(App.getString("public_key"));
        Log.d("PublicKey", "Public Key: " + bytesToHex(publicKey));

        byte[] encryptedImageData = SealedBox.seal(imageData, publicKey);
        saveImageToFile(encryptedImageData);
    }

    private void handleGalleryResult(@Nullable Intent data) {
        try {
            if (data != null && data.getData() != null) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), data.getData());
                byte[] imageData = bitmapToByteArray(bitmap);


                byte[] publicKey = hexToBytes(App.getString(App.PREF_PUBLIC_KEY));

                byte[] encryptedImageData = SealedBox.seal(imageData, publicKey);
                saveImageToFile(encryptedImageData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    private void saveImageToFile(byte[] data) {
        File directory = new File(requireContext().getFilesDir(), "cryptology");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String fileName = "image_" + System.currentTimeMillis() + ".dat";
        File file = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            Toast.makeText(requireContext(), "Image saved: " + file.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
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