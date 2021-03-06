package com.at.checkinglicense;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.at.checkinglicense.license.AESObfuscator;
import com.at.checkinglicense.license.LicenseChecker;
import com.at.checkinglicense.license.LicenseCheckerCallback;
import com.at.checkinglicense.license.ServerManagedPolicy;

public class MainActivity extends AppCompatActivity {

    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[]{
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,
            89
    };

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // A handler on the UI thread.
    private Handler mHandler = new Handler();

    private TextView mTxtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                getString(R.string.app_public_key));

        mTxtStatus = (TextView) findViewById(R.id.txtStatus);
        findViewById(R.id.btnCheckingLicense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChecker.checkAccess(mLicenseCheckerCallback);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChecker.onDestroy();
    }

    private void displayResult(final String message) {
        mHandler.post(new Runnable() {
            public void run() {
                mTxtStatus.setText(message);
            }
        });
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {

            Log.d("MainActivity", "policyReason " + policyReason);

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            displayResult("License OK");

        }

        public void dontAllow(int policyReason) {

            Log.d("MainActivity", "policyReason " + policyReason);

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            displayResult("License not OK");

            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
        }

        public void applicationError(int errorCode) {

            Log.d("MainActivity", "errorCode " + errorCode);

            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            displayResult("Error " + errorCode);

            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
        }
    }

}
