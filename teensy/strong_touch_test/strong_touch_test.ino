#define LL 18 // lower left (with cable on bottom)
#define LR 15 // lower right
#define UL 20 // upper left
#define UR 14 // upper right

#define TOP A2 // top plate

#define NUM_READS 50

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

void setup() {
  pinMode(LL, OUTPUT);
  pinMode(LR, OUTPUT);
  pinMode(UL, OUTPUT);
  pinMode(UR, OUTPUT);
}

void loop() {
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
  Serial.print(val);
  Serial.print(": ");
  
  digitalWrite(UL, HIGH);
  digitalWrite(LL, HIGH);
  digitalWrite(UR, LOW);
  digitalWrite(LR, LOW);
  delay(10);
  x = 0;
  for(int i = 0; i < NUM_READS; i++) {
    x += analogRead(TOP);
  }
  
  Serial.print(x);
  Serial.print(", ");
  
  digitalWrite(UL, HIGH);
  digitalWrite(LL, LOW);
  digitalWrite(UR, HIGH);
  digitalWrite(LR, LOW);
  delay(10);
  y = 0;
  for(int i = 0; i < NUM_READS; i++) {
    y += analogRead(TOP);
  }
  Serial.println(y);
}
