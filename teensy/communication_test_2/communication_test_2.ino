#include "ST7565.h"

ST7565 glcd(2,3,4,5,6);

namespace LCD {
  int line=0, column=0;
  char buf[40];
  
  void clearLine() {
    glcd.fillrect(0, (line % 8) * 8, 127, (line % 8) * 8 + 8, WHITE);
  }
  
  void drawCharRaw(uint8_t c) {
    glcd.drawchar((column * 6) + 1, line % 8, c);
  }
  
  void drawChar(uint8_t c) {
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
  
  void printNoDisplay(const char* string) {
    while(*string) {
      drawChar(*string++);
    }
  }
  
  void printNoDisplay(double x, int numDigits, int format) {
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
  
  void print(double x, int numDigits, int format) {
    printNoDisplay(x, numDigits, format);
    glcd.display();
  }
  
  void println(double x, int numDigits, int format) {
    printNoDisplay(x, numDigits, format);
    drawChar('\n');
    glcd.display();
  }
  
  void printNoDisplay(int x, int format) {
    int base = 10;
    if(format == HEX) base = 16;
    if(format == DEC) base = 10;
    if(format == OCT) base = 8;
    if(format == BIN) base = 2;
    itoa(x, buf, base);
    for(char *c = buf; *c; c++) {
      drawChar(*c);
    }
  }
  
  void print(int x, int format) {
    printNoDisplay(x, format);
    glcd.display();
  }
  
  void println(int x, int format) {
    printNoDisplay(x, format);
    drawChar('\n');
    glcd.display();
  }
  
  void print(char c) {
    drawChar(c);
    glcd.display();
  }
  
  void println(char c) {
    drawChar(c);
    drawChar('\n');
    glcd.display();
  }
  
  void print(const char* string) {
    printNoDisplay(string);
    glcd.display();
  }
  
  void println(const char* string) {
    printNoDisplay(string);
    drawChar('\n');
    glcd.display();
  }
  
  void println() {
    drawChar('\n');
  }
}

double tsConstants[2][3]; // needs to be a 64-bit IEE754 floating-point value

void setup() {
  Serial.begin(9600);
  while(!Serial);
  
  glcd.begin(0x18);
  glcd.clear();
  
  for(int i = 0; i < 256; i++) {
    LCD::print((byte)i, HEX);
  }
  
  //waitForComputerConnection();
  //doCommand(0x42);
}

void doCommand(int cmdId) {
  Serial.write(0x80 | cmdId);
  while(Serial.read() != 0x06);
}

void loop() {
  while(!Serial.available());
  LCD::print(Serial.read(),HEX);
}

void waitForComputerConnection() {
  LCD::println("Connecting");
  while(!Serial.dtr()); // wait for connection
  LCD::println("Handshaking");
  while(Serial.read() != 0x06) { // wait for acnowledgement
    Serial.write(0x85); // noop
    delay(100);
  }
  while(Serial.available()) Serial.read(); // clear input buffer
  LCD::println("Connected!");
}
