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
    private List<File> imageFiles;

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

            // Decode image file into Bitmap
            File imageFile = imageFiles.get(position);
            Bitmap bitmap = decodeFile(imageFile);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            }

            return convertView;
        }

        // ViewHolder pattern for better GridView performance
        private static class ViewHolder {
            ImageView imageView;
        }

        // Decode image file into Bitmap
        private Bitmap decodeFile(File imageFile) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
