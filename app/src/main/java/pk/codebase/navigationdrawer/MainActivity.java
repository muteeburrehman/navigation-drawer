package pk.codebase.navigationdrawer;

import static io.xconn.cryptology.SealedBox.seal;
import static io.xconn.cryptology.SealedBox.sealOpen;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.nio.charset.StandardCharsets;

import io.xconn.cryptology.CryptoSign;
import io.xconn.cryptology.KeyPair;
import io.xconn.cryptology.SealedBox;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    public static final String PREF_PUBLIC_KEY = "public_key";
    public static final String PREF_PRIVATE_KEY = "private_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve saved keys if they exist
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String publicKey = sharedPreferences.getString(PREF_PUBLIC_KEY, null);
        String privateKey = sharedPreferences.getString(PREF_PRIVATE_KEY, null);

        // Generate keys if not already saved
        if (publicKey == null || privateKey == null) {
            KeyPair keyPair = SealedBox.generateKeyPair();
            publicKey = new String(keyPair.getPublicKey(), StandardCharsets.UTF_8);
            privateKey = new String(keyPair.getPrivateKey(), StandardCharsets.UTF_8);

            // Save the keys in shared preferences
            saveKeysInPreferences(publicKey, privateKey);
        }


        KeyPair keyPair1 = SealedBox.generateKeyPair();

        String toEncrypt = "Hellooo";
        byte[] encrypted = seal(toEncrypt.getBytes(StandardCharsets.UTF_8), keyPair1.getPublicKey());

        byte[] decrypted = sealOpen(encrypted, keyPair1.getPrivateKey());


        System.out.println("-------------------------" + new String(decrypted, StandardCharsets.UTF_8));


        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Replace the fragment with CameraFragment when the activity is created
        Fragment cameraFragment = new CameraFragment();
        fragmentManager.beginTransaction().replace(R.id.frameLayout, cameraFragment).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.menu_camera) {
                fragment = new CameraFragment();
            } else if (item.getItemId() == R.id.menu_gallery) {
                fragment = new GalleryFragment();
            }

            if (fragment != null) {
                fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
                return true;
            }
            return false;
        });
    }

    // Method to save keys in SharedPreferences
    private void saveKeysInPreferences(String publicKey, String privateKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_PUBLIC_KEY, publicKey);
        editor.putString(PREF_PRIVATE_KEY, privateKey);
        editor.apply();
    }
}
