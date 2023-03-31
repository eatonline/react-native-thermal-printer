package com.reactnativethermalprinter.TelpoPrinter.TextParser;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParser;
import com.dantsu.escposprinter.textparser.PrinterTextParserLine;
import com.dantsu.escposprinter.textparser.PrinterTextParserTag;
import com.reactnativethermalprinter.TelpoPrinter.TelpoPrinter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextParser {
  private String text;
  TelpoPrinter printer;

  public TextParser(String text, TelpoPrinter printer) {
    this.text = text;
    this.printer = printer;
  }

  public ArrayList<TextLine> textLines() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException {
    String[] stringLines = this.text.split("\n|\r\n");
    ArrayList<TextLine> textLines = new ArrayList<>();
    for (String line : stringLines) {
      textLines.add(new TextLine(line, printer));
    }
    return textLines;
  }

  public class TextLine {
    String lineText;
    PrinterTextParserLine textParserLine;

    public TextLine(String text, TelpoPrinter printer) throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException {
      this.lineText = text;
      textParserLine = new PrinterTextParserLine(new PrinterTextParser(printer), lineText);
    }

    public ArrayList<TextColumn> getColumns() {
      return toTextColumns(sortedColumns(lineText));
    }

    private ArrayList<String> sortedColumns(String textLine) {
    //  textLine = textLine.replaceAll("(?m)^\\s+$", "");
      Pattern pattern = Pattern.compile(PrinterTextParser.getRegexAlignTags());
      Matcher matcher = pattern.matcher(textLine);
      ArrayList<String> columnsList = new ArrayList<>();

      int lastPosition = 0;
      while (matcher.find()) {
        int startPosition = matcher.start();
        if (startPosition > 0) {
          columnsList.add(textLine.substring(lastPosition, startPosition));
        }
        lastPosition = startPosition;
      }
      // aligned columns
      columnsList.add(textLine.substring(lastPosition));

      return columnsList;
    }

    private ArrayList<TextColumn> toTextColumns(ArrayList<String> columns) {
      ArrayList<TextColumn> textColumn = new ArrayList<>();
      for (String column : columns) {
        textColumn.add(new TextColumn(column, textParserLine));
      }

      return textColumn;
    }
  }

  public class TextColumn {
    private int fontWeight;
    public String text;
    public String textAlignment = PrinterTextParser.TAGS_ALIGN_LEFT;
    private PrinterTextParserLine textParserLine;
    public ArrayList<TextElement> elements = new ArrayList<>();

    public TextColumn(String textColumn, PrinterTextParserLine textParserLine) {
      this.textParserLine = textParserLine;
      if (textColumn.length() > 2) {
        // remove text align symbols from text
        switch (textColumn.substring(0, 3).toUpperCase()) {
          case "[" + PrinterTextParser.TAGS_ALIGN_LEFT + "]":
          case "[" + PrinterTextParser.TAGS_ALIGN_CENTER + "]":
          case "[" + PrinterTextParser.TAGS_ALIGN_RIGHT + "]":
            textAlignment = textColumn.substring(1, 2).toUpperCase();
            textColumn = textColumn.substring(3);
            break;
        }
      }

      String trimmedTextColumn = textColumn.trim();
      boolean isImgOrBarcodeLine = false;

      if (this.textParserLine.getNbrColumns() == 1 && trimmedTextColumn.indexOf("<") == 0) {
        // =================================================================
        // Image or Barcode Lines
        int openTagIndex = trimmedTextColumn.indexOf("<"),
          openTagEndIndex = trimmedTextColumn.indexOf(">", openTagIndex + 1) + 1;

        if (openTagIndex < openTagEndIndex) {
          PrinterTextParserTag textParserTag = new PrinterTextParserTag(trimmedTextColumn.substring(openTagIndex, openTagEndIndex));

          switch (textParserTag.getTagName()) {
            case PrinterTextParser.TAGS_IMAGE:
            case PrinterTextParser.TAGS_BARCODE:
            case PrinterTextParser.TAGS_QRCODE:
              String closeTag = "</" + textParserTag.getTagName() + ">";
              int closeTagPosition = trimmedTextColumn.length() - closeTag.length();

              if (trimmedTextColumn.substring(closeTagPosition).equals(closeTag)) {
                switch (textParserTag.getTagName()) {
                  case PrinterTextParser.TAGS_IMAGE:
                    this.appendImage(textAlignment, trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                    break;
                  case PrinterTextParser.TAGS_BARCODE:
                    this.appendBarcode(textAlignment, textParserTag.getAttributes(), trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                    break;
                  case PrinterTextParser.TAGS_QRCODE:
                    this.appendQRCode(textAlignment, textParserTag.getAttributes(), trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                    break;
                }
                isImgOrBarcodeLine = true;
              }
              break;
          }
        }
      }

      if (!isImgOrBarcodeLine) {
        parseStringTags(textColumn, this.textParserLine.getTextParser());
      }

    }

    private void parseStringTags(String textColumn, PrinterTextParser textParser) {
      int offset = 0;
      while (true) {
        int openTagIndex = textColumn.indexOf("<", offset), closeTagIndex = -1;

        if (openTagIndex != -1) {
          closeTagIndex = textColumn.indexOf(">", openTagIndex);
        } else {
          openTagIndex = textColumn.length();
        }

        this.appendString(textColumn.substring(offset, openTagIndex));

        if (closeTagIndex == -1) {
          break;
        }

        closeTagIndex++;
        PrinterTextParserTag textParserTag = new PrinterTextParserTag(textColumn.substring(openTagIndex, closeTagIndex));

        if (PrinterTextParser.isTagTextFormat(textParserTag.getTagName())) {
          if (textParserTag.isCloseTag()) {
            switch (textParserTag.getTagName()) {
              case PrinterTextParser.TAGS_FORMAT_TEXT_BOLD:
                textParser.dropTextBold();
                break;
              case PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE:
                textParser.dropLastTextUnderline();
                textParser.dropLastTextDoubleStrike();
                break;
              case PrinterTextParser.TAGS_FORMAT_TEXT_FONT:
                textParser.dropLastTextSize();
                textParser.dropLastTextColor();
                textParser.dropLastTextReverseColor();
                break;
            }
          } else {
            switch (textParserTag.getTagName()) {
              case PrinterTextParser.TAGS_FORMAT_TEXT_BOLD:
                textParser.addTextBold(EscPosPrinterCommands.TEXT_WEIGHT_BOLD);
                break;
              case PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE:
                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE)) {
                  switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE)) {
                    case PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL:
                      textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE);
                      textParser.addTextDoubleStrike(textParser.getLastTextDoubleStrike());
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE:
                      textParser.addTextUnderline(textParser.getLastTextUnderline());
                      textParser.addTextDoubleStrike(EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_ON);
                      break;
                  }
                } else {
                  textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE);
                  textParser.addTextDoubleStrike(textParser.getLastTextDoubleStrike());
                }
                break;
              case PrinterTextParser.TAGS_FORMAT_TEXT_FONT:
                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                  switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL:
                    default:
                      textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_TALL:
                      textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_HEIGHT);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_WIDE:
                      textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG:
                      textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG);
                      break;
                  }
                } else {
                  textParser.addTextSize(textParser.getLastTextSize());
                }

                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                  switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BLACK:
                    default:
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK);
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK:
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK);
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_RED:
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED);
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF);
                      break;
                    case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED:
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED);
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON);
                      break;
                  }
                } else {
                  textParser.addTextColor(textParser.getLastTextColor());
                  textParser.addTextReverseColor(textParser.getLastTextReverseColor());
                }
                break;
            }
          }
          offset = closeTagIndex;
        } else {
          this.appendString("<");
          offset = openTagIndex + 1;
        }
      }
    }

    private TextParser.TextColumn appendString(String text) {
      PrinterTextParser textParser = this.textParserLine.getTextParser();
      text = text.replaceAll("(?m)^\\s+$", "");
      return this.appendString(text, textParser.getLastTextSize(), textParser.getLastTextColor(), textParser.getLastTextBold(), textParser.getLastTextUnderline(), textParser.getLastTextDoubleStrike());
    }

    private TextParser.TextColumn appendString(String text, byte[] textSize, byte[] textColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) {
      TextElement textElement = new TextElement(text, textSize, textColor, textBold, textUnderline, textDoubleStrike);
      return this.appendElement(textElement);
    }


    private TextParser.TextColumn appendElement(TextElement element) {
      elements.add(element);
      return this;
    }

    private void appendQRCode(String alignment, Hashtable<String, String> attributes, String text) {

    }

    private void appendBarcode(String alignment, Hashtable<String, String> attributes, String text) {

    }

    private void appendImage(String alignment, String image) {

    }
  }

}
