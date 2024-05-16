package pk.codebase.navigationdrawer;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Display the captured image in an ImageView (assuming you have an ImageView in activity_gallery.xml)
        ImageView imageView = findViewById(R.id.imageView);
        // Load the image from file and set it to the ImageView
        // Implement this part based on how you saved the image bytes to a file

    }
}
