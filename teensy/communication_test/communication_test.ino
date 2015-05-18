#include "ST7565.h"

ST7565 glcd(2,3,4,5,6);

namespace LCD {
  int line=0, column=0;
  char buf[40];
  
  void clearLine() {
    glcd.fillrect(0, (line % 8) * 8, 127, (line % 8) * 8 + 8, WHITE);
  }
  
  void drawCharRaw(uint8_t c) {
    glcd.drawchar((column * 6) + 1, line % 8, c, BLACK);
  }
  
  void drawChar(char c_char) {
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

namespace SerialTunnel {
  void begin(long speed) {
    Serial.begin(speed);
    while(!Serial);
  }
  
  int available() {
    return Serial.available();
  }
  
  int read() {
    int b = Serial.read();
    if(b == -1) return -1;
    
    if(b < 32 || b > (95+32)) {
      // error
      LCD::print("recieved ");
      LCD::print(b, HEX);
      LCD::println("; OOB");
    }
    return b-32;
  }
  
  double readDouble(double *retlocd) {
    uint64_t *retlocl = (uint64_t *)retlocd;
    *retlocl = 0;
    for(int shift = 0; shift < 64; shift += 6) {
      uint8_t b = read();
      *retlocl |= ((b & 0x3f) << shift);
    }
    return *retlocl;
  }
  
  double readDouble() {
    double d = 0;
    readDouble(&d);
    return d;
  }
  
  void flush() {
    Serial.flush();
  }
  
  void send_now() {
    Serial.send_now();
  }
  
  void writeInternal(uint8_t b) {
    Serial.write(b+32);
  }
  
  void sendByte(uint8_t b) {
    if(b < 0 || b > 95) {
      LCD::print("oob byte: ");
      LCD::print(b, HEX);
    }
    writeInternal(b);
  }
  
  void sendData(uint8_t dataValue) {
    if(dataValue < 0 || dataValue > 0x40) {
      LCD::print("invalid data: ");
      LCD::print(dataValue, HEX);
    }
    writeInternal(dataValue);
  }
  
  void sendCommand(uint8_t cmdId) {
    if(cmdId < 0 || cmdId > 0x1e) {
      LCD::print("invalid command :");
      LCD::print(cmdId, HEX);
    }
    writeInternal(cmdId | 0x40);
  }
  
  void doCommand(uint8_t cmdId) {
    sendCommand(cmdId);
    while(read() != 0x06);
  }
  
  uint8_t dtr() {
    return Serial.dtr();
  }
}

double tsConstants[2][3]; // needs to be a 64-bit IEE754 floating-point value

void setup() {
  SerialTunnel::begin(9600);
  
  glcd.begin(0x18);
  glcd.clear();
  
  waitForComputerConnection();
  
  for(int i = 0; i < 4; i++) {
    int x = random(40000);
    int y = random(40000);
    LCD::print("(");
    LCD::print(x,DEC);
    LCD::print(",");
    LCD::print(y,DEC);
    LCD::println(")");
    SerialTunnel::doCommand(0x10);
    sendXY(x, y);
  }
  
  LCD::println("recieving TS");
  SerialTunnel::doCommand(0x11);
  recieveTsConstants();
  LCD::println("done");
}

void loop() {}

void recieveTsConstants() {
  for(int i = 0; i < 2; i++) {
    for(int j = 0; j < 3; j++) {
      SerialTunnel::readDouble(&(tsConstants[i][j]));
    }
  }
}

/*void recieveDouble(double *doublePointer) {
  for(byte *bytePointer = (byte *)(doublePointer); (bytePointer - (byte *)doublePointer) < sizeof(double); bytePointer++) {
    *bytePointer = SerialTunnel::read();
    LCD::print(*bytePointer,HEX);
    LCD::print(" ");
  }
  LCD::println();
  //LCD::println(*doublePointer,12,DEC);
}*/

void waitForComputerConnection() {
  LCD::println("Connecting");
  while(!(SerialTunnel::dtr())); // wait for connection
  LCD::println("Handshaking");
  while(SerialTunnel::read() != 0x06) { // wait for acnowledgement
    SerialTunnel::sendCommand(0x05); // noop
    delay(100);
  }
  while(SerialTunnel::available()) SerialTunnel::read(); // clear input buffer
  LCD::println("Connected!");
}

void sendXY(int x, int y) {
  SerialTunnel::sendData((x >> 12) & 0x3f); // 6 bits of x
  SerialTunnel::sendData((x >> 6 ) & 0x3f); // 6 bits of x
  SerialTunnel::sendData( x        & 0x3f); // 6 bits of x
  
  SerialTunnel::sendData((y >> 12) & 0x3f); // 6 bits of y
  SerialTunnel::sendData((y >> 6 ) & 0x3f); // 6 bits of y
  SerialTunnel::sendData( y        & 0x3f); // 6 bits of y
  
  SerialTunnel::send_now();
}
