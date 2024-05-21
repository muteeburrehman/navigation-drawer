package pk.codebase.navigationdrawer;

import static pk.codebase.navigationdrawer.util.App.PREF_PRIVATE_KEY;
import static pk.codebase.navigationdrawer.util.App.PREF_PUBLIC_KEY;
import static pk.codebase.navigationdrawer.util.App.saveBoolean;
import static pk.codebase.navigationdrawer.util.App.saveString;
import static pk.codebase.navigationdrawer.util.Helpers.bytesToHex;
import static pk.codebase.navigationdrawer.util.Helpers.convertTo32Bytes;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.Objects;

import io.xconn.cryptology.KeyPair;
import io.xconn.cryptology.SealedBox;
import io.xconn.cryptology.SecretBox;
import pk.codebase.navigationdrawer.fragment.CameraFragment;
import pk.codebase.navigationdrawer.fragment.GalleryFragment;
import pk.codebase.navigationdrawer.util.App;

public class MainActivity extends AppCompatActivity {

    Dialog dialog;
    Button btnDialogOk;


    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!App.getBoolean("isDialogShown")) {
            showDialog();
        }
        fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

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


    private void showDialog() {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_dialog_box);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        btnDialogOk = dialog.findViewById(R.id.btnDialogOK);

        btnDialogOk.setOnClickListener(v -> {
            EditText enterPasswordEditText = dialog.findViewById(R.id.enterPassword);
            String enteredPassword = enterPasswordEditText.getText().toString().trim();

            if (enteredPassword.isEmpty()) {
                enterPasswordEditText.setError("Please enter a password");
                enterPasswordEditText.requestFocus();
            } else {
                KeyPair keyPair = SealedBox.generateKeyPair();
                String publicKey = bytesToHex(keyPair.getPublicKey());
                saveString(PREF_PUBLIC_KEY, publicKey);

                byte[] nonce = SecretBox.generateNonce();
                saveString("nonce", bytesToHex(nonce));

                byte[] encryptedPrivateKey = SecretBox.box(nonce, keyPair.getPrivateKey(),
                        Objects.requireNonNull(convertTo32Bytes(enteredPassword)));
                saveString(PREF_PRIVATE_KEY, bytesToHex(encryptedPrivateKey));

                saveBoolean("isDialogShown", true);

                dialog.dismiss();

            }
        });

        dialog.show();
    }

}
