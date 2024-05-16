package pk.codebase.navigationdrawer;

import static io.xconn.cryptology.SealedBox.sealOpen;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PRIVATE_KEY;
import static pk.codebase.navigationdrawer.MainActivity.PREF_PUBLIC_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;



import javax.crypto.Cipher;

import io.xconn.cryptology.SealedBox;

public class GalleryFragment extends Fragment {

    private GridView gridView;
    private List<File> imageFiles;
    private byte[] privateKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = view.findViewById(R.id.gridView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPrivateKey();
        loadImages();
    }

    private void loadPrivateKey() {
        // Retrieve the private key from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String privateKeyString = sharedPreferences.getString("private_key", null);
        if (privateKeyString != null) {
            privateKey = privateKeyString.getBytes(StandardCharsets.UTF_8);
        } else {
            Toast.makeText(requireContext(), "Private key not found", Toast.LENGTH_SHORT).show();
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

        ImageAdapter(Context context, List<File> imageFiles) {
            this.context = context;
            this.imageFiles = imageFiles;

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
                convertView = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Decode and decrypt image file into Bitmap
            File imageFile = imageFiles.get(position);
            Bitmap bitmap = decryptImageData(imageFile);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            }

            return convertView;
        }

        // ViewHolder pattern for better GridView performance
        private static class ViewHolder {
            ImageView imageView;
        }

        // Decrypt image file into Bitmap
        private Bitmap decryptImageData(File imageFile) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                byte[] encryptedData = new byte[(int) imageFile.length()];
                fis.read(encryptedData);
                fis.close();

                // Decrypt the encrypted image data using the private key
                byte[] decryptedData = SealedBox.sealOpen(encryptedData, getPrivateKey(context));

                // Convert decrypted data to Bitmap
                return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private byte[] getPrivateKey(Context context) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String privateKeyString = sharedPreferences.getString(PREF_PRIVATE_KEY, "");
            System.out.println("-----bNfs------" + privateKeyString.getBytes(StandardCharsets.UTF_8));
            return hexToBytes(privateKeyString);
        }

        public static byte[] hexToBytes(String hexString) {
            int len = hexString.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i + 1), 16));
            }
            return data;
        }

    }
}
