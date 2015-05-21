#include "LCD.h"
#include "SerialTunnel.h"

LCD lcd(2,3,4,5,6);
SerialTunnel serialTunnel(lcd);

double tsConstants[2][3]; // needs to be a 64-bit IEE754 floating-point value

void setup() {
  lcd.begin(0x18);
  serialTunnel.begin(9600);
  
  waitForComputerConnection();
  
  for(int i = 0; i < 4; i++) {
    int x = random(40000);
    int y = random(40000);
    lcd.print("(");
    lcd.print(x);
    lcd.print(",");
    lcd.print(y);
    lcd.println(")");
    serialTunnel.doCommand(0x10);
    sendXY(x, y);
  }
  
  lcd.println("recieving TS");
  serialTunnel.doCommand(0x11);
  recieveTsConstants();
  /*lcd.println("done");
  lcd.print("{");
  for(int i = 0; i < 2; i++) {
    lcd.print("{");
    for(int j = 0; j < 3; j++) {
      lcd.print(tsConstants[i][j],2,DEC);
      if(j < 2) lcd.print(", ");
    }
    lcd.print("}");
    if(i < 1) lcd.print(", ");
  }
  lcd.print("}");*/
}

void loop() {}

void recieveTsConstants() {
  for(int i = 0; i < 2; i++) {
    for(int j = 0; j < 3; j++) {
      serialTunnel.readDouble(&(tsConstants[i][j]));
    }
  }
}

/*void recieveDouble(double *doublePointer) {
  for(byte *bytePointer = (byte *)(doublePointer); (bytePointer - (byte *)doublePointer) < sizeof(double); bytePointer++) {
    *bytePointer = serialTunnel.read();
    lcd.print(*bytePointer,HEX);
    lcd.print(" ");
  }
  lcd.println();
  //lcd.println(*doublePointer,12,DEC);
}*/

void waitForComputerConnection() {
  lcd.println("Connecting");
  while(!(serialTunnel.dtr())); // wait for connection
  lcd.println("Handshaking");
  while(serialTunnel.read() != 0x06) { // wait for acnowledgement
    serialTunnel.sendCommand(0x05); // noop
    delay(100);
  }
  while(serialTunnel.available()) serialTunnel.read(); // clear input buffer
  lcd.println("Connected!");
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
