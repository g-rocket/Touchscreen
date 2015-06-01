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
  int base;
  int len;
  switch(format) {
    case HEX: base = 16; len = sizeof(b)*2; break;
    case DEC: base = 10; len = (int)(log(b)); break;
    case OCT: base =  8; len = (int)(log(b) / log(8)); break;
    case BIN: base =  2; len = sizeof(b)*8; break;
    default:
      print("invalid format");
      return;
  }
  if(len <= 0) len = 1;
  buf[len] = '\0';
  for(int i = len - 1; i >= 0; i--) {
    uint8_t digit = b % base;
    buf[i] = ((digit <= 9)? (digit + '0'): (digit-10 + 'a'));
    b /= base;
  }
  drawString(buf);
}

void LCD::drawByte(uint64_t b, int format) {
  int base;
  int len;
  switch(format) {
    case HEX: base = 16; len = sizeof(b)*2; break;
    case DEC: base = 10; len = (int)(log(b)); break;
    case OCT: base =  8; len = (int)(log(b) / log(8)); break;
    case BIN: base =  2; len = sizeof(b)*8; break;
    default:
      print("invalid format");
      return;
  }
  if(len <= 0) len = 1;
  buf[len] = '\0';
  for(int i = len - 1; i >= 0; i--) {
    uint8_t digit = b % base;
    buf[i] = ((digit <= 9)? (digit + '0'): (digit-10 + 'a'));
    b /= base;
  }
  drawString(buf);
}

void LCD::drawByte(int b, int format) {
  int base;
  int len;
  switch(format) {
    case HEX: base = 16; len = sizeof(b)*2; break;
    case DEC: base = 10; len = (int)(log(b)); break;
    case OCT: base =  8; len = (int)(log(b) / log(8)); break;
    case BIN: base =  2; len = sizeof(b)*8; break;
    default:
      print("invalid format");
      return;
  }
  if(len <= 0) len = 1;
  buf[len] = '\0';
  for(int i = len - 1; i >= 0; i--) {
    uint8_t digit = b % base;
    buf[i] = ((digit <= 9)? (digit + '0'): (digit-10 + 'a'));
    b /= base;
  }
  drawString(buf);
}
