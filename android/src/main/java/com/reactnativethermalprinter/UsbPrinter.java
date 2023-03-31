package com.reactnativethermalprinter;

import android.content.Context;
import android.util.Log;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public class UsbPrinter extends DeviceConnection {

  @Override
  public DeviceConnection connect() throws EscPosConnectionException {

    return null;
  }

  @Override
  public DeviceConnection disconnect() {
    return null;
  }

  static class UsbPrintConfig {
    final float printerWidthMM;
    final int printerDpi;
    final int numbCharsPerLine;

    UsbPrintConfig(int printerDpi, float printerWidthMM, int numbCharsPerLine) {
      this.printerDpi = printerDpi;
      this.printerWidthMM = printerWidthMM;
      this.numbCharsPerLine = numbCharsPerLine;
    }
  }


}
