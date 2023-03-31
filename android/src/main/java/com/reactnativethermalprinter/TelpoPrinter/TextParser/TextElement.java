package com.reactnativethermalprinter.TelpoPrinter.TextParser;

public class TextElement {
  public String text;
  public byte[] textSize;
  public byte[] textColor;
  public byte[] textBold;
  public byte[] textUnderline;
  public byte[] textDoubleStrike;

  public TextElement(String text, byte[] textSize, byte[] textColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) {
    this.text = text;
    this.textSize = textSize;
    this.textColor = textColor;
    this.textBold = textBold;
    this.textUnderline = textUnderline;
    this.textDoubleStrike = textDoubleStrike;
  }
}
