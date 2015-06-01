#include "SerialTunnel.h"

void SerialTunnel::begin(long speed) {
  Serial.begin(speed);
  while(!Serial);
}

int SerialTunnel::available() {
  return Serial.available();
}

int SerialTunnel::read() {
  int b = Serial.read();
  if(b == -1) return -1;
  
  if(b < 32 || b > (95+32)) {
    // error
    lcd.print("recieved ");
    lcd.print(b, HEX);
    lcd.println("; OOB");
  }
  return b-32;
}

double SerialTunnel::readDouble(double *retlocd) {
  uint64_t *retlocl = (uint64_t *)retlocd;
  *retlocl = 0;
  lcd.print("0x");
  for(int shift = 0; shift < 64; shift += 6) {
    uint8_t b = read();
    *retlocl |= ((b & 0x3f) << shift);
  }
  lcd.print(*retlocl, HEX);
  lcd.print(",");
  return *retlocd;
}

double SerialTunnel::readDouble() {
  double d = 0;
  readDouble(&d);
  return d;
}

void SerialTunnel::flush() {
  Serial.flush();
}

void SerialTunnel::send_now() {
  Serial.send_now();
}

void SerialTunnel::writeInternal(uint8_t b) {
  Serial.write(b+32);
}

void SerialTunnel::sendByte(uint8_t b) {
  if(b < 0 || b > 95) {
    lcd.print("oob byte: ");
    lcd.print(b, HEX);
  }
  writeInternal(b);
}

void SerialTunnel::sendData(uint8_t dataValue) {
  if(dataValue < 0 || dataValue > 0x40) {
    lcd.print("invalid data: ");
    lcd.print(dataValue, HEX);
  }
  writeInternal(dataValue);
}

void SerialTunnel::sendCommand(uint8_t cmdId) {
  if(cmdId < 0 || cmdId > 0x1e) {
    lcd.print("invalid command :");
    lcd.print(cmdId, HEX);
  }
  writeInternal(cmdId | 0x40);
}

void SerialTunnel::doCommand(uint8_t cmdId) {
  sendCommand(cmdId);
  while(read() != 0x06);
}

uint8_t SerialTunnel::dtr() {
  return Serial.dtr();
}
