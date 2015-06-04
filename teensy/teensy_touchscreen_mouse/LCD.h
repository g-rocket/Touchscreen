#ifndef LCD_h
#define LCD_h

#include "Arduino.h"
#include "ST7565.h"

class LCD {
  public:
    LCD(int8_t SID, int8_t SCLK, int8_t AO, int8_t RST, int8_t CS): glcd(SID,SCLK,AO,RST,CS) {}
    LCD(int8_t SID, int8_t SCLK, int8_t AO, int8_t RST): glcd(SID,SCLK,AO,RST) {}
    
    void begin(uint8_t contrast);
    
    void print(char c) { drawChar(c); glcd.display(); }
    void print(const char* string) { drawString(string); glcd.display(); }
    void println(char c) { drawChar(c); drawChar('\n'); glcd.display(); }
    void println(const char* string) { drawString(string); drawChar('\n'); glcd.display(); }
    void println() { drawChar('\n'); glcd.display(); }
    
    void print(double x, int numDigits = 2, int format = DEC) { drawFloat(x, numDigits, format); glcd.display(); }
    void println(double x, int numDigits = 2, int format = DEC) { drawFloat(x, numDigits, format); drawChar('\n'); glcd.display(); }
    
    void print(byte x) { drawInt(x); glcd.display(); }
    void print(int  x) { drawInt(x); glcd.display(); }
    void print(long x) { drawInt(x); glcd.display(); }
    void println(byte x) { drawInt(x); drawChar('\n'); glcd.display(); }
    void println(int  x) { drawInt(x); drawChar('\n'); glcd.display(); }
    void println(long x) { drawInt(x); drawChar('\n'); glcd.display(); }
    
    void print(uint8_t  b, int format = HEX) { drawByte(b, format); glcd.display(); }
    void print(uint64_t b, int format = HEX) { drawByte(b, format); glcd.display(); }
    void print(int      b, int format      ) { drawByte(b, format); glcd.display(); }
    void println(uint8_t  b, int format = HEX) { drawByte(b, format); drawChar('\n'); glcd.display(); }
    void println(uint64_t b, int format = HEX) { drawByte(b, format); drawChar('\n'); glcd.display(); }
    void println(int      b, int format      ) { drawByte(b, format); drawChar('\n'); glcd.display(); }
    
  private:
    int line=0, column=0;
    char buf[40];
    ST7565 glcd;
    
    void clearLine();
    void drawCharRaw(uint8_t c);
    
    void drawChar(char c);
    void drawString(const char* string);
    
    void drawFloat(double x, int numDigits = 2, int format = DEC);
    
    void drawInt(byte x);
    void drawInt(int  x);
    void drawInt(long x);
    
    void drawByte(uint8_t b, int format = HEX);
    void drawByte(uint64_t b, int format = HEX);
    void drawByte(int b, int format);
};

#endif
