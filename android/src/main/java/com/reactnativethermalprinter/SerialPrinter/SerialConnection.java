package com.reactnativethermalprinter.SerialPrinter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SerialConnection extends DeviceConnection {

  private Context context;
  UsbSerialPort port;

  public SerialConnection(Context context) {
    this.context = context;
  }

  @Override
  public DeviceConnection connect() {

    // Find all available drivers from attached devices.
    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    UsbDevice usbDevice = manager.getDeviceList().values().iterator().next();

    if (usbDevice == null) return this;

    int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.android.example.USB_PERMISSION"), flags);
    IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
    // context.registerReceiver(usbReceiver, filter);

    manager.requestPermission(usbDevice, permissionIntent);


    List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
    if (availableDrivers.isEmpty()) {
      //  return this;
    }

    // Open a connection to the first available driver.
    // UsbSerialDriver driver = availableDrivers.get(0);
    // CdcAcmSerialDriver, FtdiSerialDriver
    UsbSerialDriver driver = new FtdiSerialDriver(usbDevice);
    UsbDeviceConnection connection = manager.openDevice(driver.getDevice());


    this.port = driver.getPorts().get(0); // Most devices have just one port (port 0)
    try {
      port.open(connection);
      port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return this;
  }

  @Override
  public boolean isConnected() {
    return this.port != null;
  }

  @Override
  public DeviceConnection disconnect() {
    this.port = null;
    return this;
  }

  @Override
  public void send(int addWaitingTime) {
    if (this.port == null || this.data == null) return;

    try {
      this.port.write(this.data, addWaitingTime);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
