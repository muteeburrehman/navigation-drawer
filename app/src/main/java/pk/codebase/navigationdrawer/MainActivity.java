package pk.codebase.navigationdrawer;

import static pk.codebase.navigationdrawer.CameraFragment.hexToBytes;
import static pk.codebase.navigationdrawer.utils.App.PREF_PRIVATE_KEY;
import static pk.codebase.navigationdrawer.utils.App.PREF_PUBLIC_KEY;
import static pk.codebase.navigationdrawer.utils.App.saveBoolean;
import static pk.codebase.navigationdrawer.utils.App.saveString;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import io.xconn.cryptology.KeyPair;
import io.xconn.cryptology.SealedBox;
import io.xconn.cryptology.SecretBox;
import pk.codebase.navigationdrawer.utils.App;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnDialogOk;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the dialog has been shown previously using App.getPreferenceManager()
        if (!App.getBoolean("isDialogShown")) {
            showDialog();
        }

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

    // Method to show the dialog
    private void showDialog() {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        // Initialize btnDialogOk button
        btnDialogOk = dialog.findViewById(R.id.btnDialogOK);

        // Set OnClickListener for btnDialogOk
        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve entered password from EditText
                EditText enterPasswordEditText = dialog.findViewById(R.id.enterPassword);
                String enteredPassword = enterPasswordEditText.getText().toString().trim();

                // Check if the entered password is empty
                if (enteredPassword.isEmpty()) {
                    // If the password is empty, show an error message
                    enterPasswordEditText.setError("Please enter a password");
                    enterPasswordEditText.requestFocus(); // Focus on the EditText to prompt user input
                } else {
                    KeyPair keyPair = SealedBox.generateKeyPair();
                    String publicKey = bytesToHex(keyPair.getPublicKey());
                    saveString(PREF_PUBLIC_KEY, publicKey);

                    byte[] nonce = SecretBox.generateNonce();
                    saveString("nonce", bytesToHex(nonce));

                    byte[] encryptedPrivateKey = SecretBox.box(nonce, keyPair.getPrivateKey(), convertTo32Bytes(enteredPassword));
                    saveString(PREF_PRIVATE_KEY, bytesToHex(encryptedPrivateKey));

                    // Set the flag to indicate the dialog has been shown
                    saveBoolean("isDialogShown", true);

                    // Dismiss the dialog
                    dialog.dismiss();

                    // Log the entered password
                    Log.d("password", "My Password " + enteredPassword);
                }
            }
        });

        dialog.show();
    }

    public static byte[] convertTo32Bytes(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // If the hash is less than 32 bytes, we pad it with zeros
            byte[] result = new byte[32];
            if (hash.length >= 32) {
                System.arraycopy(hash, 0, result, 0, 32);
            } else {
                System.arraycopy(hash, 0, result, 0, hash.length);
            }

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
