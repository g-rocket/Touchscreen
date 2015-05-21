#include "Arduino.h"
#include "LCD.h"

class SerialTunnel {
  public:
    SerialTunnel(LCD _lcd): lcd(_lcd) {}
    void begin(long speed);
    int available();
    int read();
    double readDouble(double *retlocd);
    double readDouble();
    void flush();
    void send_now();
    void sendByte(uint8_t b);
    void sendData(uint8_t dataValue);
    void sendCommand(uint8_t cmdId);
    void doCommand(uint8_t cmdId);
    uint8_t dtr();
    
  private:
    void writeInternal(uint8_t b);
    LCD lcd;
};
