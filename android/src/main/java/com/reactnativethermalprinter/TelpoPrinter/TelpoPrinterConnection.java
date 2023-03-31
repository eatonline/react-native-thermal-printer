package com.reactnativethermalprinter.TelpoPrinter;

import android.content.Context;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;

public class TelpoPrinterConnection extends DeviceConnection {
  private UsbThermalPrinter printer;
  private boolean isConnected = TelpoPrinter.IS_TESTING;

  public TelpoPrinterConnection(Context context) {
    this.printer = new UsbThermalPrinter(context);

  }

  public UsbThermalPrinter getPrinter() {
    return this.printer;
  }

  @Override
  public void write(byte[] bytes) {
    super.write(bytes);
  }

  @Override
  public boolean isConnected() {
    return this.isConnected;
  }

  @Override
  public DeviceConnection connect() throws EscPosConnectionException {
    if (TelpoPrinter.IS_TESTING) {
      return this;
    }
    try {
      printer.start(0);
      this.isConnected = true;
      return this;
    } catch (TelpoException e) {
      throw new EscPosConnectionException(e.getMessage());
    }
  }


  @Override
  public DeviceConnection disconnect() {
    if (TelpoPrinter.IS_TESTING) {
      this.isConnected = false;
      return this;
    }
    printer.stop();
    this.isConnected = false;
    return this;
  }
}
