#include "LCD.h"

void LCD::begin(uint8_t contrast) {
  glcd.begin(contrast);
  glcd.clear();
}
  
void LCD::clearLine() {
  glcd.fillrect(0, (line % 8) * 8, 127, (line % 8) * 8 + 8, WHITE);
}

void LCD::drawCharRaw(uint8_t c) {
  glcd.drawchar((column * 6) + 1, line % 8, c, BLACK);
}

void LCD::drawChar(char c_char) {
  uint8_t c = *((uint8_t *)(&c_char));
  switch(c) {
    case '\r': column = 0; break;
    case '\n': column = 0; line++; clearLine(); break;
    case 0x0b: line++; break;
    case 0x08: column--; drawCharRaw(' '); break;
    case 0x7f: drawCharRaw(' '); break;
    default: drawCharRaw(c); column++;
  }
  if(column >= 21) {
    column = 0;
    line++;
    clearLine();
  }
}

void LCD::drawString(const char* string) {
  while(*string) {
    drawChar(*string++);
  }
}

void LCD::drawFloat(double x, int numDigits, int format) {
  int base = 10;
  if(format == HEX) base = 16;
  if(format == DEC) base = 10;
  if(format == OCT) base = 8;
  if(format == BIN) base = 2;
  
  if(x < 0) drawChar('-');
  int shift = pow(base,numDigits);
  int val = (int)(x*shift);
  itoa(abs(val/shift), buf, base);
  for(char *c = buf; *c; c++) {
    drawChar(*c);
  }
  drawChar('.');
  itoa(abs(val%shift), buf, base);
  int len = strlen(buf);
  for(int i = 0; i < numDigits-len; i++) {
    drawChar('0');
  }
  for(char *c = buf; *c; c++) {
    drawChar(*c);
  }
}



void LCD::drawInt(byte x) {
  itoa(x, buf, 10);
  drawString(buf);
}

void LCD::drawInt(int x) {
  itoa(x, buf, 10);
  drawString(buf);
}

void LCD::drawInt(long x) {
  itoa(x, buf, 10);
  drawString(buf);
}



void LCD::drawByte(uint8_t b, int format) {
  int fmt;
  int cpb; // chars per byte
  switch(format) {
    case HEX: fmt = 16; cpb = 2; break;
    case DEC: fmt = 10; cpb = 0; break;
    case OCT: fmt =  8; cpb = 3; break;
    case BIN: fmt =  2; cpb = 8; break;
    default:
      print("invalid format");
      return;
  }
  int expectedLen = sizeof(b) * cpb;
  itoa(b, buf, fmt);
  int len = strlen(buf);
  if(len < expectedLen) {
    int dif = expectedLen - len;
    for(int i = len-1; i >= 0; i--) {
      buf[i+dif] = buf[i];
    }
    for(int i = 0; i < dif; i++) {
      buf[i] = '0';
    }
  }
  drawString(buf);
}

void LCD::drawByte(uint64_t b, int format) {
  int fmt;
  int cpb; // chars per byte
  switch(format) {
    case HEX: fmt = 16; cpb = 2; break;
    case DEC: fmt = 10; cpb = 0; break;
    case OCT: fmt =  8; cpb = 3; break;
    case BIN: fmt =  2; cpb = 8; break;
    default:
      print("invalid format");
      return;
  }
  int expectedLen = sizeof(b) * cpb;
  itoa(b, buf, fmt);
  int len = strlen(buf);
  if(len < expectedLen) {
    int dif = expectedLen - len;
    for(int i = len-1; i >= 0; i--) {
      buf[i+dif] = buf[i];
    }
    for(int i = 0; i < dif; i++) {
      buf[i] = '0';
    }
  }
  drawString(buf);
}

void LCD::drawByte(int b, int format) {
  int fmt;
  int cpb; // chars per byte
  switch(format) {
    case HEX: fmt = 16; cpb = 2; break;
    case DEC: fmt = 10; cpb = 0; break;
    case OCT: fmt =  8; cpb = 3; break;
    case BIN: fmt =  2; cpb = 8; break;
    default:
      print("invalid format");
      return;
  }
  int expectedLen = sizeof(b) * cpb;
  itoa(b, buf, fmt);
  int len = strlen(buf);
  if(len < expectedLen) {
    int dif = expectedLen - len;
    for(int i = len-1; i >= 0; i--) {
      buf[i+dif] = buf[i];
    }
    for(int i = 0; i < dif; i++) {
      buf[i] = '0';
    }
  }
  drawString(buf);
}
