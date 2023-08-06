package com.reactnativethermalprinter.TelpoPrinter;

import android.util.Log;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.textparser.PrinterTextParser;
import com.reactnativethermalprinter.TelpoPrinter.TextParser.TextElement;
import com.reactnativethermalprinter.TelpoPrinter.TextParser.TextParser;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;

public class TelpoPrinterCommands extends EscPosPrinterCommands {
  TelpoPrinterConnection printerConnection;

  public TelpoPrinterCommands(TelpoPrinterConnection printerConnection) {
    super(printerConnection);
    this.printerConnection = printerConnection;
  }

  protected UsbThermalPrinter printer() {
    return this.printerConnection.getPrinter();
  }


  @Override
  public EscPosPrinterCommands cutPaper() throws EscPosConnectionException {
    return this;
  }

  private void applyTextSize(byte[] textSize) throws TelpoException {
    if (textSize == TEXT_SIZE_BIG) {
      printer().setTextSize(34);
    } else if (textSize == TEXT_SIZE_NORMAL) {
      printer().setTextSize(24);
    } else if (textSize == TEXT_SIZE_DOUBLE_HEIGHT) {
      printer().setTextSize(28);
      printer().enlargeFontSize(1, 2);
    } else if (textSize == TEXT_SIZE_DOUBLE_WIDTH) {
      printer().setTextSize(26);
      printer().enlargeFontSize(2, 1);
    } else {
      printer().setTextSize(22);
    }
  }

  private void applyTextColor(byte[] textColor) throws TelpoException {
    // ignore
  }

  private void applyTextDoubleStrike(byte[] doubleStrike) {
    // ignore
  }

  private void applyTextAlignment(String alignment) throws TelpoException {
    switch (alignment) {
      case PrinterTextParser.TAGS_ALIGN_LEFT:
        printer().setAlgin(UsbThermalPrinter.ALGIN_LEFT);
        break;
      case PrinterTextParser.TAGS_ALIGN_RIGHT:
        printer().setAlgin(UsbThermalPrinter.ALGIN_RIGHT);
        break;
      case PrinterTextParser.TAGS_ALIGN_CENTER:
        printer().setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
        break;
    }
  }

  private void applyTextBold(byte[] textBold) throws TelpoException {
    printer().setBold(textBold == TEXT_WEIGHT_BOLD);
  }

  private void applyTextUnderline(byte[] textUnderline) throws TelpoException {
    printer().setUnderline(textUnderline == TEXT_UNDERLINE_ON);
  }

  @Override
  public EscPosPrinterCommands newLine(byte[] align) throws EscPosConnectionException {
    try {
      printer().endLine();
    } catch (TelpoException e) {
      throw new EscPosConnectionException(e.getMessage());
    }
    return this;
  }

  @Override
  public EscPosPrinterCommands reset() {
    try {
      printer().reset();
      this.printer().start(0);
    } catch (TelpoException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) throws EscPosEncodingException {

    if (TelpoPrinter.IS_TESTING) {
      System.out.println(text);
      return this;
    }
    try {
      this.applyTextSize(textSize);
      this.applyTextColor(textColor);
      this.applyTextBold(textBold);
      this.applyTextUnderline(textUnderline);
      this.applyTextDoubleStrike(textDoubleStrike);

      printer().addString(text);
      printer().printString();
    } catch (TelpoException e) {
      throw new EscPosEncodingException(e.getMessage());
    }

    return this;
  }

  public void endLine() throws TelpoException {
    if (TelpoPrinter.IS_TESTING) return;
    printer().endLine();
  }

  public void walkPaper(int lines) throws TelpoException {
    if (TelpoPrinter.IS_TESTING) return;
    printer().walkPaper(lines);
  }

  public TelpoPrinterCommands printColumn(TextParser.TextColumn column) throws EscPosEncodingException {
    if (TelpoPrinter.IS_TESTING) {
      String columnContent = "---- \n Alignment: " + column.textAlignment + "\n" +
        "Elements:";

      for (TextElement s : column.elements) {
        columnContent += "TextSize: " + s.textSize +
          "TextColor: " + s.textColor +
          "Bold: " + s.textBold +
          "Underline: " + s.textUnderline +
          "DoubleStrike: " + s.textDoubleStrike +
          "Text: " + s.text;
        Log.d("telpoPrinter", columnContent);
      }
      return this;
    }
    try {
      for (TextElement s : column.elements) {

        // remove newline string:
        applyTextAlignment(column.textAlignment);
        this.applyTextSize(s.textSize);
        this.applyTextColor(s.textColor);
        this.applyTextBold(s.textBold);
        this.applyTextUnderline(s.textUnderline);
        this.applyTextDoubleStrike(s.textDoubleStrike);

        printer().addSringOneLine(s.text);
        if (column.elements.indexOf(s) == column.elements.size() - 1) {
          // last item
          printer().addString("");
        }

      }
    } catch (TelpoException e) {
      throw new EscPosEncodingException(e.getMessage());
    }
    return this;
  }

}
