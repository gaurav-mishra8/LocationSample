package com.greenbotsite.locationexplorer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by gaurav on 14/2/17.
 */

public class PermissionsChecker {


    private final Context context;

    public PermissionsChecker(Context context) {
        this.context = context;
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }


}
