package com.reactnativethermalprinter.SerialPrinter;

public class SerialPrintConfig {
  final float printerWidthMM;
  final int printerDpi;
  final int numbCharsPerLine;

  public SerialPrintConfig(int printerDpi, float printerWidthMM, int numbCharsPerLine) {
    this.printerDpi = printerDpi;
    this.printerWidthMM = printerWidthMM;
    this.numbCharsPerLine = numbCharsPerLine;
  }
}
