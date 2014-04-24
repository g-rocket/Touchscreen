#define LL 15 // lower left (with cable on bottom)
#define LR 13 // lower right
#define UL 16 // upper left
#define UR 12 // upper right

#define TOP A7 // top plate

int state = 0;
// 0 -> not touched
// 1 -> detecting touch type
// 2 -> detecting if drag or right click
// 3 -> moving mouse
// 4 -> dragging mouse
// 5 -> configuring

int x = 0;
int y = 0;
int lastX = 10;
int lastY = 10;

void setup(){
  Serial.begin(9600);
  pinMode(LL, OUTPUT);
  pinMode(LR, OUTPUT);
  pinMode(UL, OUTPUT);
  pinMode(UR, OUTPUT);
}

void loop(){
  readTS(false);
  printLoc();
}

void printLoc(){
  Serial.print("(");
  Serial.print(x);
  Serial.print(", ");
  Serial.print(y);
  Serial.println(")");
}

boolean pressed(){
  pinMode(TOP, INPUT_PULLUP);
  digitalWrite(UL, LOW);
  digitalWrite(LL, LOW);
  digitalWrite(UR, LOW);
  digitalWrite(UL, LOW);
  delay(10);
  boolean pressed = !digitalRead(TOP);
  pinMode(TOP, INPUT);
  return pressed;
}

void readTS(boolean interpolate){
  digitalWrite(UL, HIGH);
  digitalWrite(LL, HIGH);
  digitalWrite(UR, LOW);
  digitalWrite(LR, LOW);
  delay(10);
  x = lerp(lastX,analogRead(TOP)/*map(analogRead(TOP), 805, 218, 0, 1024)*/,interpolate?.8:1);
  digitalWrite(UL, HIGH);
  digitalWrite(LL, LOW);
  digitalWrite(UR, HIGH);
  digitalWrite(LR, LOW);
  delay(10);
  y = lerp(lastY,analogRead(TOP)/*map(analogRead(TOP), 232, 770, 0, 768)*/,interpolate?.8:1);
}

int lerp(int a, int b, float value){
  return a+((b-a)*value);
}




