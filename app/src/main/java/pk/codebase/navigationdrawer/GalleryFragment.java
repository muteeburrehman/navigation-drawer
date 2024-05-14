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
        List<String> imagePaths = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                // Add file paths to the list
                imagePaths.add(file.getAbsolutePath());
            }
        }

        // Create custom adapter and set it to GridView
        ImageAdapter adapter = new ImageAdapter(requireContext(), imagePaths);
        gridView.setAdapter(adapter);
    }

    // Custom adapter for displaying images in GridView
    private static class ImageAdapter extends BaseAdapter {

        private Context context;
        private List<String> imagePaths;

        ImageAdapter(Context context, List<String> imagePaths) {
            this.context = context;
            this.imagePaths = imagePaths;
        }

        @Override
        public int getCount() {
            return imagePaths.size();
        }

        @Override
        public Object getItem(int position) {
            return imagePaths.get(position);
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

            // Load image into ImageView using BitmapFactory
            Bitmap bitmap = loadImageFromFile(imagePaths.get(position));
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            }

            return convertView;
        }

        // Load Bitmap object from file
        private Bitmap loadImageFromFile(String filePath) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                return BitmapFactory.decodeFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        // ViewHolder pattern for better GridView performance
        private static class ViewHolder {
            ImageView imageView;
        }
    }
}
