package com.reactnativethermalprinter.TelpoPrinter;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.reactnativethermalprinter.TelpoPrinter.TextParser.TextParser;
import com.telpo.tps550.api.TelpoException;

public class TelpoPrinter extends EscPosPrinter {
  final static boolean IS_TESTING = false;
  private TelpoPrinterCommands printer;

  public TelpoPrinter(TelpoPrinterCommands printer, int printerDpi, float printerWidthMM, int printerNbrCharactersPerLine) throws EscPosConnectionException {
    super(printer, printerDpi, printerWidthMM, printerNbrCharactersPerLine);
    this.printer = printer;
  }

  @Override
  public EscPosPrinter printFormattedText(String text, int dotsFeedPaper) throws EscPosParserException, EscPosEncodingException, EscPosBarcodeException {
    TextParser textParser = new TextParser(text, this);
    try {
      for (TextParser.TextLine line : textParser.textLines()) {
        for (TextParser.TextColumn column : line.getColumns()) {
          printer.printColumn(column);
        }
    //    printer.printer().printString();
      }
      printer.printer().walkPaper(3);
      printer.printer().addString("  ");
      printer.printer().printString();
    } catch (TelpoException e) {
      throw new RuntimeException(e);
    }

    return this;
  }


}
