#include "ST7565.h"

ST7565 glcd(2,3,4,5,6);

namespace LCD {
  int line=0, column=0;
  char buf[40];
  
  void clearLine() {
    glcd.fillrect(column * 6 + 1, (line % 8) * 8, 127, (line % 8) * 8 + 8, WHITE);
  }
  
  void drawchar(int li, int co, uint8_t ch) {
    glcd.drawchar((co * 6) + 1, li % 8, ch);
  }
  
  void drawChar(uint8_t c) {
    switch(c) {
      case '\r': column = 0; break;
      case '\n': column = 0; line++; clearLine(); break;
      case 0x0b: line++; break;
      case 0x08: column--; drawchar(line, column, ' '); break;
      case 0x7f: drawchar(line, column, ' '); break;
      default: drawchar(line, column, c); column++;
    }
    if(column > 20) {
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

#define LL 18 // lower left (with cable on bottom)
#define LR 15 // lower right
#define UL 20 // upper left
#define UR 14 // upper right

#define TOP A2 // top plate

#define NUM_READS 40

unsigned long touchStartTime;
int touchStartX;
int touchStartY;

double tsConstants[2][3]; // needs to be a 64-bit IEE754 floating-point value

boolean configured = false;

int detectDelay = 200; // number of millis to wait while detecting touch type
int moveDistanceSq = 40; // number of pixels that is a "move"

int32_t x = 0;
int32_t y = 0;
int32_t lastX = 10;
int32_t lastY = 10;

boolean pressed;
boolean lastPressed;
boolean clicked;
boolean released;

boolean moving;
boolean isRightClick;

boolean configuring;

void setup(){
  Serial.begin(9600);
  while(!Serial);
  
  glcd.begin(0x18);
  glcd.clear();
  
  //Mouse.screenSize(1024,768);
  Mouse.screenSize(1920, 1080);
  pinMode(LL, OUTPUT);
  pinMode(LR, OUTPUT);
  pinMode(UL, OUTPUT);
  pinMode(UR, OUTPUT);
  
  configure();
}

void loop(){
  readTS();
  
  if(configuring) {
    LCD::println("configuring in loop");
    int cmd = 0;
    while(cmd != 0x81) { // while not done
      LCD::println("waiting for input");
      while(!Serial.available()); // wait for input
      cmd = Serial.read();
      LCD::print("recieved 0x");
      LCD::println(cmd, HEX);
      if(cmd == 0x80) { // read
        LCD::print("waiting ");
        while(!readTS());
        printXY();
        LCD::println();
        sendXY();
      }
    }
    LCD::println("recieving configuration");
    recieveTsConstants();
    LCD::println("done configuring");
    configuring = false;
    configured = true;
    return;
  }
  
  if(clicked) {
    touchStartTime = millis();
    touchStartX = x;
    touchStartY = y;
    moving = false;
    Mouse.moveTo(x, y);
  }
  
  if(pressed && moving) {
    Mouse.moveTo(x, y);
  }
  
  if(pressed && !moving) {
    if(sq(x-touchStartX)+sq(y-touchStartX) >= moveDistanceSq) {
      moving = true;
      if(millis() > touchStartTime + detectDelay) {
        Mouse.set_buttons(1,0,0);
      }
    } else if(millis() > touchStartTime + detectDelay) {
      isRightClick = true;
    }
  }
  
  if(released) {
    if(!moving) {
      if(isRightClick) {
        Mouse.set_buttons(0,0,1);
      } else {
        Mouse.set_buttons(1,0,0);
      }
    }
    Mouse.set_buttons(0,0,0);
  }
}

void printXY() {
  LCD::print("(");
  LCD::print(x, DEC);
  LCD::print(",");
  LCD::print(y, DEC);
  LCD::print(")");
}

void configure() {
  LCD::println("Connecting...");
  while(!Serial.dtr()); // wait for connection
  LCD::println("Handshaking");
  while(Serial.read() != 0x06) { // wait for agknowledgement
    Serial.write(0x85); // noop
    delay(100);
  }
  while(Serial.available()) Serial.read(); // clear input buffer
  LCD::println("requesting configure");
  Serial.write(0x84); // start configure
  while(Serial.read() != 0x06); // wait for acnowledgement
  configuring = true;
  LCD::println("Starting configure");
}

boolean readTS(){
  lastX = x;
  lastY = y;
  lastPressed = pressed;
  
  pinMode(TOP, INPUT_PULLUP);
  digitalWrite(UL, LOW);
  digitalWrite(LL, LOW);
  digitalWrite(UR, LOW);
  digitalWrite(UL, LOW);
  delay(10);
  int val = 0;
  for(int i = 0; i < NUM_READS; i++) {
    val += analogRead(TOP);
  }
  pinMode(TOP, INPUT);
  pressed = val < NUM_READS*512;
  clicked = pressed && !lastPressed;
  released = lastPressed && !pressed;
  if(!pressed) return false;
  
  digitalWrite(UL, HIGH);
  digitalWrite(LL, HIGH);
  digitalWrite(UR, LOW);
  digitalWrite(LR, LOW);
  delay(10);
  x = 0;
  for(int i = 0; i < NUM_READS; i++) {
    x += analogRead(TOP);
  }
  
  digitalWrite(UL, HIGH);
  digitalWrite(LL, LOW);
  digitalWrite(UR, HIGH);
  digitalWrite(LR, LOW);
  delay(10);
  y = 0;
  for(int i = 0; i < NUM_READS; i++) {
    y += analogRead(TOP);
  }
  
  if(configured) transformXY();
  
  return true;
}

void transformXY() {
  int tmpX = x, tmpY = y;
  x = round(tsConstants[0][0] + (tsConstants[0][1]*tmpX) + (tsConstants[0][2]*tmpY));
  y = round(tsConstants[1][0] + (tsConstants[1][1]*tmpY) + (tsConstants[1][2]*tmpY));
}

void recieveTsConstants() {
  for(int i = 0; i < 2; i++) {
    for(int j = 0; j < 3; j++) {
      byte *tsConstant = (byte*)(&(tsConstants[i][j]));
      for(int k = 0; k < 8; k++) {
        tsConstant[k] = Serial.read();
      }
      LCD::println(tsConstants[i][j],12,DEC);
    }
  }
}

void sendXY() {
  Serial.write(((x >> 12) & 0x3f) | 0x40); // 01b, then 6 bits of x
  Serial.write(((x >> 6 ) & 0x3f) | 0x40); // 01b, then 6 bits of x
  Serial.write(( x        & 0x3f) | 0x40); // 01b, then 6 bits of x
  
  Serial.write(((y >> 12) & 0x3f) | 0x40); // 01b, then 6 bits of y
  Serial.write(((y >> 6 ) & 0x3f) | 0x40); // 01b, then 6 bits of y
  Serial.write(( y        & 0x3f) | 0x40); // 01b, then 6 bits of y
  
  Serial.send_now();
}

int lerp(int a, int b, float value){
  return a+((b-a)*value);
}




