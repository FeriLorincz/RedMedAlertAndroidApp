package com.feri.redmedalertandroidapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class SamsungHealthPermissionUtil {
    private static final String TAG = "SamsungHealthPermUtil";

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openSamsungHealthSettings(Activity activity) {
        // Deschide setările aplicației Samsung Health
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", "com.sec.android.app.shealth", null);
            intent.setData(uri);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Nu s-au putut deschide setările Samsung Health", e);
            // Alternativ, încearcă să deschizi direct aplicația Samsung Health
            try {
                Intent intent = activity.getPackageManager().getLaunchIntentForPackage("com.sec.android.app.shealth");
                if (intent != null) {
                    activity.startActivity(intent);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Nu s-a putut deschide Samsung Health", ex);
            }
        }
    }

    public static void enableDeveloperMode(Activity activity) {
        // Instruiește utilizatorul cum să activeze modul developer
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle("Activare mod developer Samsung Health")
                .setMessage("Pentru a permite accesul, trebuie să activați modul developer în Samsung Health:\n\n" +
                        "1. Deschideți Samsung Health\n" +
                        "2. Apăsați pe ⋮ (meniu) > Settings > About Samsung Health\n" +
                        "3. Apăsați de 10 ori rapid pe versiune\n" +
                        "4. Activați Developer Mode\n" +
                        "5. Activați Developer Mode for Data Read\n\n" +
                        "După ce ați terminat, reveniți la această aplicație.")
                .setPositiveButton("Deschide Samsung Health", (dialog, id) -> {
                    try {
                        Intent intent = activity.getPackageManager().getLaunchIntentForPackage("com.sec.android.app.shealth");
                        if (intent != null) {
                            activity.startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Nu s-a putut deschide Samsung Health", e);
                    }
                })
                .setNegativeButton("Anulează", (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }
}