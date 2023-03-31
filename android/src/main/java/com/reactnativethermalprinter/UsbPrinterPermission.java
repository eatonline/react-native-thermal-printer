package com.reactnativethermalprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbPrinterPermission {

  private PrinterPermissionCallback callback;

  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

  private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (device != null) {
              callback.onResult(true);
            } else {
              callback.onResult(false);
            }
          } else {
            Log.d("RN_PRINTER", "permission denied for device " + device);
            callback.onResult(false);
          }
        }
      }
    }
  };


  public void request(UsbDevice device, Context activity, PrinterPermissionCallback callback) {
    this.callback = callback;

    UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    activity.registerReceiver(usbReceiver, filter);
    usbManager.requestPermission(device, permissionIntent);
  }

}
