package pk.codebase.navigationdrawer;

import static io.xconn.cryptology.SealedBox.sealOpen;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PRIVATE_KEY;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PUBLIC_KEY;
import static pk.codebase.navigationdrawer.MainActivity.convertTo32Bytes;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import io.xconn.cryptology.SealedBox;
import io.xconn.cryptology.SecretBox;


import javax.crypto.Cipher;

import io.xconn.cryptology.SealedBox;
import pk.codebase.navigationdrawer.utils.App;

public class GalleryFragment extends Fragment {

    private GridView gridView;
    private List<File> imageFiles;
    private byte[] privateKey;
    private Dialog passwordDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = view.findViewById(R.id.gridView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showPasswordDialog();
    }

    private void showPasswordDialog() {
        passwordDialog = new Dialog(requireContext());
        passwordDialog.setContentView(R.layout.dialog_password);
        passwordDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        passwordDialog.setCancelable(false);

        EditText passwordEditText = passwordDialog.findViewById(R.id.enterYourPassword);
        Button submitButton = passwordDialog.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                decryptPrivateKey(password);
            } else {
                Toast.makeText(requireContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            }
        });

        passwordDialog.show();
    }

    private void decryptPrivateKey(String password) {
        // Retrieve the encrypted private key and nonce from SharedPreferences

         byte[] privateKey = App.getString("private_key").getBytes();
         byte[] nonceKey = App.getString("nonce").getBytes();

        if (privateKey != null && nonceKey != null) {
            byte[] encryptedPrivateKey = privateKey;
            byte[] nonce = nonceKey;

            // Decrypt the private key using the entered password and the stored nonce
            byte[] decryptedPrivateKey = SecretBox.boxOpen(nonce, encryptedPrivateKey,  convertTo32Bytes(password));

            if (decryptedPrivateKey != null) {
                // Successfully decrypted the private key, proceed to load images
                privateKey = decryptedPrivateKey;
                loadImages();
                passwordDialog.dismiss();
            } else {
                // Incorrect password
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Private key or nonce not found
            Toast.makeText(requireContext(), "Private key or nonce not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        // Create a directory named "cryptology" in internal storage
        File directory = new File(requireContext().getFilesDir(), "cryptology");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Retrieve image files from the "cryptology" directory
        imageFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // Add image files to the list
                    imageFiles.add(file);
                }
            }
        }

        // Create custom adapter and set it to GridView
        ImageAdapter adapter = new ImageAdapter(requireContext(), imageFiles);
        gridView.setAdapter(adapter);
    }

    // Custom adapter for displaying images in GridView
    private static class ImageAdapter extends BaseAdapter {

        private Context context;
        private List<File> imageFiles;
        private LayoutInflater inflater;

        ImageAdapter(Context context, List<File> imageFiles) {
            this.context = context;
            this.imageFiles = imageFiles;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return imageFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return imageFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gallery_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Set a placeholder image while the actual image is being loaded
            holder.imageView.setImageResource(R.drawable.placeholder);

            // Decode and decrypt image file into Bitmap asynchronously
            File imageFile = imageFiles.get(position);
            decodeAndDecryptImageDataAsync(imageFile, holder.imageView);

            return convertView;
        }

        // ViewHolder pattern for better GridView performance
        private static class ViewHolder {
            ImageView imageView;
        }

        // Decrypt image file into Bitmap asynchronously
        private void decodeAndDecryptImageDataAsync(final File imageFile, final ImageView imageView) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    return decryptImageData(imageFile);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        // Handle failed image loading
                        imageView.setImageResource(R.drawable.error);
                    }
                }
            }.execute();
        }

        // Decrypt image file into Bitmap
        private Bitmap decryptImageData(File imageFile) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                byte[] encryptedData = new byte[(int) imageFile.length()];
                fis.read(encryptedData);
                fis.close();

                // Decrypt the encrypted image data using the private key
                byte[] decryptedData = SealedBox.sealOpen(encryptedData, hexToBytes(PREF_PRIVATE_KEY));

                // Convert decrypted data to Bitmap
                return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    public static byte[] convertTo24Bytes(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Create a 24-byte array
            byte[] result = new byte[24];
            if (hash.length >= 24) {
                System.arraycopy(hash, 0, result, 0, 24);
            } else {
                System.arraycopy(hash, 0, result, 0, hash.length);
                // If hash is less than 24 bytes, pad the remaining bytes with zeros
                for (int i = hash.length; i < 24; i++) {
                    result[i] = 0;
                }
            }

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}