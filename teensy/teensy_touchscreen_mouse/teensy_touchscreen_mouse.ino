#include "LCD.h"
#include "SerialTunnel.h"

LCD lcd(2,3,4,5,6);
SerialTunnel serialTunnel(lcd);

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
  serialTunnel.begin(9600);
  while(!Serial);
  
  lcd.begin(0x18);
  
  //Mouse.screenSize(1024,768);
  Mouse.screenSize(1920, 1080);
  pinMode(LL, OUTPUT);
  pinMode(LR, OUTPUT);
  pinMode(UL, OUTPUT);
  pinMode(UR, OUTPUT);
  
  waitForComputerConnection();
  
  configure();
}

void waitForComputerConnection() {
  lcd.println("Connecting");
  while(!(serialTunnel.dtr())); // wait for connection
  lcd.println("Handshaking");
  while(serialTunnel.read(false) != 0x06) { // wait for acnowledgement
    serialTunnel.sendCommand(0x05); // noop
    delay(100);
  }
  while(serialTunnel.available()) serialTunnel.read(false); // clear input buffer
  lcd.println("Connected!");
}

void configure() {
  lcd.println("requesting configure");
  serialTunnel.doCommand(0x4);
  configuring = true;
  lcd.println("Starting configure");
}

void loop(){
  readTS();
  
  if(configuring) {
    lcd.println("configuring in loop");
    int cmd = 0;
    while(cmd != 0x21) { // while not done
      lcd.println("waiting for input");
      while(!serialTunnel.available()); // wait for input
      cmd = serialTunnel.read(true);
      lcd.print("recieved 0x");
      lcd.println(cmd, HEX);
      if(cmd == 0x20) { // read
        lcd.print("waiting ");
        while(!readTS());
        printXY();
        lcd.println();
        sendXY(x, y);
      }
    }
    lcd.println("recieving configuration");
    recieveTsConstants();
    lcd.println("done configuring");
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
  lcd.print("(");
  lcd.print(x);
  lcd.print(",");
  lcd.print(y);
  lcd.print(")");
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
      serialTunnel.readDouble(&(tsConstants[i][j]));
    }
  }
}

void sendXY(int x, int y) {
  serialTunnel.sendData((x >> 12) & 0x3f); // 6 bits of x
  serialTunnel.sendData((x >> 6 ) & 0x3f); // 6 bits of x
  serialTunnel.sendData( x        & 0x3f); // 6 bits of x
  
  serialTunnel.sendData((y >> 12) & 0x3f); // 6 bits of y
  serialTunnel.sendData((y >> 6 ) & 0x3f); // 6 bits of y
  serialTunnel.sendData( y        & 0x3f); // 6 bits of y
  
  serialTunnel.send_now();
}

int lerp(int a, int b, float value){
  return a+((b-a)*value);
}




