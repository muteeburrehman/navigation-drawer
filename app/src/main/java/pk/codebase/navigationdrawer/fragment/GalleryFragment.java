package pk.codebase.navigationdrawer.fragment;

import static pk.codebase.navigationdrawer.util.Helpers.convertTo32Bytes;
import static pk.codebase.navigationdrawer.util.Helpers.hexToBytes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.xconn.cryptology.SealedBox;
import io.xconn.cryptology.SecretBox;
import pk.codebase.navigationdrawer.R;
import pk.codebase.navigationdrawer.util.App;

public class GalleryFragment extends Fragment {

    private GridView gridView;
    private static byte[] privateKey;
    private Dialog passwordDialog;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
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
        Objects.requireNonNull(passwordDialog.getWindow()).setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordDialog.setCancelable(false);

        EditText passwordEditText = passwordDialog.findViewById(R.id.enterYourPassword);
        Button submitButton = passwordDialog.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                decryptPrivateKey(password);
            } else {
                Toast.makeText(requireContext(), "Please enter your password",
                        Toast.LENGTH_SHORT).show();
            }
        });

        passwordDialog.show();
    }

    private void decryptPrivateKey(String password) {
        String encryptedPrivateKeyHex = App.getString(App.PREF_PRIVATE_KEY);
        String nonceHex = App.getString("nonce");

        if (!TextUtils.isEmpty(encryptedPrivateKeyHex) && !TextUtils.isEmpty(nonceHex)) {
            byte[] encryptedPrivateKey = hexToBytes(encryptedPrivateKeyHex);
            byte[] nonce = hexToBytes(nonceHex);

            try {
                privateKey = SecretBox.boxOpen(nonce, encryptedPrivateKey,
                        Objects.requireNonNull(convertTo32Bytes(password)));
                loadImages();
                passwordDialog.dismiss();
            } catch (Exception e) {
                // Handle decryption error (e.g., incorrect password)
                Toast.makeText(requireContext(), "Incorrect password. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Private key or nonce not found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        File directory = new File(requireContext().getFilesDir(), "cryptology");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        List<File> imageFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    imageFiles.add(file);
                }
            }
        }

        ImageAdapter adapter = new ImageAdapter(requireContext(),
                imageFiles,
                executorService, mainHandler);
        gridView.setAdapter(adapter);
    }

    private static class ImageAdapter extends BaseAdapter {

        private final List<File> imageFiles;
        private final LayoutInflater inflater;
        private final ExecutorService executorService;
        private final Handler mainHandler;

        ImageAdapter(Context context,
                     List<File> imageFiles,
                     ExecutorService executorService,
                     Handler mainHandler) {
            this.imageFiles = imageFiles;
            this.inflater = LayoutInflater.from(context);
            this.executorService = executorService;
            this.mainHandler = mainHandler;
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

            holder.imageView.setImageResource(R.drawable.placeholder);

            File imageFile = imageFiles.get(position);
            decodeAndDecryptImageDataAsync(imageFile, holder.imageView);

            return convertView;
        }

        private static class ViewHolder {
            ImageView imageView;
        }

        private void decodeAndDecryptImageDataAsync(final File imageFile, final ImageView imageView)
        {
            executorService.execute(() -> {
                Bitmap bitmap = decryptImageData(imageFile);
                mainHandler.post(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.error);
                    }
                });
            });
        }

        private Bitmap decryptImageData(File imageFile) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                byte[] encryptedData = new byte[(int) imageFile.length()];
                fis.read(encryptedData);
                fis.close();

                byte[] decryptedData = SealedBox.sealOpen(encryptedData, privateKey);

                return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
