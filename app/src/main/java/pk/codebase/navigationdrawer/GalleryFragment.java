package pk.codebase.navigationdrawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = view.findViewById(R.id.gridView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImages();
    }

    private void loadImages() {
        // Create a directory named "cryptography" in internal storage
        File directory = new File(requireContext().getFilesDir(), "cryptography");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Retrieve image files from the "cryptography" directory
        File[] files = directory.listFiles();

        // Create a list to hold file paths of images
        List<byte[]> imageBytesList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                // Read image bytes from file
                byte[] imageBytes = readFileBytes(file);
                if (imageBytes != null) {
                    // Add image bytes to the list
                    imageBytesList.add(imageBytes);
                }
            }
        }

        // Create custom adapter and set it to GridView
        ImageAdapter adapter = new ImageAdapter(requireContext(), imageBytesList);
        gridView.setAdapter(adapter);
    }

    // Read bytes from file
    private byte[] readFileBytes(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Custom adapter for displaying images in GridView
    private static class ImageAdapter extends BaseAdapter {

        private List<byte[]> imageBytesList;
        private LayoutInflater inflater;

        ImageAdapter(Context context, List<byte[]> imageBytesList) {
            this.imageBytesList = imageBytesList;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return imageBytesList.size();
        }

        @Override
        public Object getItem(int position) {
            return imageBytesList.get(position);
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

            // Convert byte array to Bitmap
            byte[] imageBytes = imageBytesList.get(position);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            // Set Bitmap to ImageView
            holder.imageView.setImageBitmap(bitmap);

            return convertView;
        }

        // ViewHolder pattern for better GridView performance
        private static class ViewHolder {
            ImageView imageView;
        }
    }
}
