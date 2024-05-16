package pk.codebase.navigationdrawer;

import static android.security.KeyChain.getPrivateKey;
import static io.xconn.cryptology.SealedBox.seal;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PRIVATE_KEY;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PUBLIC_KEY;
import static pk.codebase.navigationdrawer.MainActivity.bytesToHex;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.xconn.cryptology.CryptoSign;
import io.xconn.cryptology.KeyPair;
import io.xconn.cryptology.SealedBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class CameraFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 201;
    private static final int REQUEST_CAMERA_PERMISSION = 101;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_capture).setOnClickListener(v -> dispatchTakePictureIntent());
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
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
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            byte[] imageData = bitmapToByteArray(bitmap);

//            KeyPair keypair = SealedBox.generateKeyPair();
//            System.out.println("---------------" + new String(keypair.getPublicKey()));
//            System.out.println("-------------pri----" + new String(keypair.getPrivateKey()));
            byte[] encryptedImageData = SealedBox.seal(imageData, getPublicKey(requireContext()));
            saveImageToFile(encryptedImageData);
            // Encrypt a message using the public key
//           String message = "Hello, World!";
//           byte[] encryption = SealedBox.seal(message.getBytes(), keypair.getPublicKey());
//            // Decrypt the message using the private key
//           byte[] decryption = SealedBox.sealOpen(encryption, keypair.getPrivateKey());
//            // Convert decrypted bytes back to string
//            String decryptedMessage = new String(decryption);
//
//            System.out.println("----Original message: "+ message);
//            System.out.println("---Encrypted message: " + bytesToHex(encryption) );
//            System.out.println("---Decrypted message: "+ decryptedMessage);








            // Decrypt the encrypted image data using the private key
            byte[] decryptedData = SealedBox.sealOpen(encryptedImageData, getPrivateKey(requireContext()));

            // Convert decrypted data to Bitmap
//            return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);

            System.out.println("*******" + decryptedData);

        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // Function to convert hexadecimal string to bytes
    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    // Function to convert bytes to hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void saveImageToFile(byte[] data) {
        File directory = new File(requireContext().getFilesDir(), "cryptology");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String fileName = "image_" + System.currentTimeMillis() + ".dat";
        File file = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
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

    private byte[] getPublicKey(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String publicKeyString = sharedPreferences.getString(PREF_PUBLIC_KEY, "");

        System.out.println("------------aaaaaaa-------------"+ publicKeyString);
        return hexToBytes(publicKeyString);
    }

    private byte[] getPrivateKey(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String privateKeyString = sharedPreferences.getString(PREF_PRIVATE_KEY, "");
        System.out.println("-------------bbbbbb---------------"+privateKeyString);
        return hexToBytes(privateKeyString);
    }
}
