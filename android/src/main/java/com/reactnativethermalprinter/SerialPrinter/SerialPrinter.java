package com.reactnativethermalprinter.SerialPrinter;

import android.util.Log;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public class SerialPrinter {

  public void print(SerialConnection serialConnection, String payload, SerialPrintConfig printConfig) throws EscPosConnectionException, EscPosEncodingException, EscPosBarcodeException, EscPosParserException {

    Log.d("RN_PRINTER", "Printing " + payload);
    EscPosPrinter printer = new EscPosPrinter(serialConnection, printConfig.printerDpi, printConfig.printerWidthMM, printConfig.numbCharsPerLine);
    printer.printAllCharsetsEncodingCharacters();
    printer
      .printFormattedText(
        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
          "[L]\n" +
          "[C]================================\n" +
          "[L]\n" +
          "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
          "[L]  + Size : S\n" +
          "[L]\n" +
          "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
          "[L]  + Size : 57/58\n" +
          "[L]\n" +
          "[C]--------------------------------\n" +
          "[R]TOTAL PRICE :[R]34.98e\n" +
          "[R]TAX :[R]4.23e\n" +
          "[L]\n" +
          "[C]================================\n" +
          "[L]\n" +
          "[L]<font size='tall'>Customer :</font>\n" +
          "[L]Raymond DUPONT\n" +
          "[L]5 rue des girafes\n" +
          "[L]31547 PERPETES\n" +
          "[L]Tel : +33801201456\n" +
          "[L]\n" +
          "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
          "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
      );

    // printer.printFormattedText(payload);
  }


}
