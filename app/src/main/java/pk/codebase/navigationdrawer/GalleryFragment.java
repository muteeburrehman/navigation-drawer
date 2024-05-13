package pk.codebase.navigationdrawer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        listView = view.findViewById(R.id.listView);
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

        // Create a list to hold Bitmap objects of images
        List<Bitmap> imageBitmapList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                // Load Bitmap object of each image
                Bitmap bitmap = loadImageFromFile(file);
                if (bitmap != null) {
                    // Add Bitmap object to the list
                    imageBitmapList.add(bitmap);
                }
            }
        }

        // Populate the ListView with images using ArrayAdapter
        ArrayAdapter<Bitmap> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, imageBitmapList);
        listView.setAdapter(adapter);
    }

    // Load Bitmap object from file
    private Bitmap loadImageFromFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
