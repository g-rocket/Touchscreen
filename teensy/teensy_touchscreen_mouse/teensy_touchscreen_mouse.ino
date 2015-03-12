#define LL 18 // lower left (with cable on bottom)
#define LR 15 // lower right
#define UL 20 // upper left
#define UR 14 // upper right

#define TOP A2 // top plate

int cmd;

unsigned long tStartTime;
int tStartX;
int tStartY;

double tsConstants[2][3];

int detectDelay = 200; // number of millis to wait while detecting touch type
int moveDistanceSq = 40; // number of pixels that is a "move"

int state = 0;
// 0 -> not touched
// 1 -> detecting touch type
// 2 -> detecting if drag or right click
// 3 -> moving mouse
// 4 -> dragging mouse
// 5 -> configuring
// 6 -> keyboard

long x = 0;
long y = 0;
int lastX = 10;
int lastY = 10;

void setup(){
  Serial.begin(9600);
  while(!Serial);
  Serial1.begin(9600);
  Serial1.println("started");
  configure();
  //Mouse.screenSize(1024,768);
  Mouse.screenSize(1920, 1080);
  pinMode(LL, OUTPUT);
  pinMode(LR, OUTPUT);
  pinMode(UL, OUTPUT);
  pinMode(UR, OUTPUT);
}

void loop(){
  //Serial.println(0x80 | state);
  /*if(Serial.available()) {
    state = 5;
    cmd = Serial.read();
  }*/
  /*boolean clicked = pressed();
   if(clicked){
   readTS(state != 0);
   }*/
  boolean clicked = pressed();
  int tmpX = x;
  int tmpY = y;
  readTS();
  clicked = clicked && pressed();
  if(!clicked) {
    x = tmpX;
    y = tmpY;
  } else {
    lastX = tmpX;
    lastY = tmpY;
  }
  //if(state != 0) Serial.println(state);
  switch(state) {
  case 0: // doing nothing
    if(clicked){
      tStartTime = millis();
      tStartX = x;
      tStartY = y;
      moveMouseToTouch();
      state = 1;
    }
    break;
  case 1: // detecting touch type
    if(sq(x-tStartX)+sq(y-tStartY) >= moveDistanceSq){
      moveMouseToTouch();
      state = 3;
    }
    if(!clicked){
      startLeftClick();
      endLeftClick();
      state = 0;
    }
    if(millis() > tStartTime + detectDelay){
      state = 2; 
    }
    break;
  case 2: // timed out, detecting if right click or drag
    if(sq(x-tStartX)+sq(y-tStartY) >= moveDistanceSq){
      startLeftClick();
      moveMouseToTouch();
      state = 4;
    }
    if(!clicked){
      startRightClick();
      endRightClick();
      state = 0;
    }
    break;
  case 3: // moving mouse
    if(clicked) moveMouseToTouch();
    else state = 0;
    break;
  case 4: // dragging mouse
    if(clicked) moveMouseToTouch();
    else{
      endLeftClick();
      state = 0;
    }
    break;
  case 5: // configuring
    Serial1.write(0x81);
    while(cmd != 0x81) { // while not done
      Serial1.write(0x82);
        while(!Serial.available()); // wait for input
        Serial1.write(0x83);
        cmd = Serial.read();
        Serial1.write(0x84);
        if(cmd == 0x80) { // read
          Serial1.write(0x85);
          while(!pressed()); // wait for click
          Serial1.write(0x86);
          readTS();
          Serial1.write(0x87);
          printXY();
          Serial1.write(0x88);
        }
      }
      state = 0;
    break;
  default:
    Serial.print("serious problem: state = ");
    Serial.println(state);
  }
}

void moveMouseToTouch(){
  //Serial.print("moving mouse to ");
  //printLoc();
  //Serial.write(B10000000); // control "move"
  //printXY();
  Mouse.moveTo(x,y);
}

void configure() {
  while(!Serial.dtr()); // wait for connection
  Serial.write(0x84); // start configure
  while(!Serial.available() || Serial.read() != 0); // wait for acnowledgement
  state = 5;
  Serial1.write(0x8f);
}

void printXY() {
  Serial.write((x & 0x3F8) >> 3); // high 7 bits of x
  Serial.write(((x & 0x007) << 4) | ((y & 0x3C0) >> 6)); // low 3 bits of x, then high 4 bits of y
  Serial.write(y & 0x03F); // low 6 bits of y
  Serial.flush();
  Serial.send_now();
}

void startLeftClick(){
  //Serial.println("starting left click");
  //Serial.write(B11000000);
  Mouse.set_buttons(1,0,0);
}

void endLeftClick(){
  //Serial.println("ending left click");
  //Serial.write(B11000001);
  Mouse.set_buttons(0,0,0);
}

void startRightClick(){
  //Serial.println("starting right click");
  //Serial.write(B11000010);
  Mouse.set_buttons(0,0,1);
}

void endRightClick(){
  //Serial.println("ending right click");
  //Serial.write(B11000011);
  Mouse.set_buttons(0,0,0);
}

void printLoc(){
  Serial1.print("(");
  Serial1.print(x);
  Serial1.print(", ");
  Serial1.print(y);
  Serial1.println(")");
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

void readTS(){
  digitalWrite(UL, HIGH);
  digitalWrite(LL, HIGH);
  digitalWrite(UR, LOW);
  digitalWrite(LR, LOW);
  delay(10);
  x = 0;
  for(int i = 0; i < 40; i++) {
    x += analogRead(TOP)<<2;
    //x = lerp(x,analogRead(TOP)/*map(analogRead(TOP), 805, 218, 0, 1024)*/,.1);
  }
  x /= 40;
  digitalWrite(UL, HIGH);
  digitalWrite(LL, LOW);
  digitalWrite(UR, HIGH);
  digitalWrite(LR, LOW);
  delay(10);
  y = 0;
  for(int i = 0; i < 40; i++) {
    y += analogRead(TOP)<<2;
    //y = lerp(x,analogRead(TOP)/*map(analogRead(TOP), 232, 770, 0, 768)*/,.1);
  }
  y /= 40;
}

void revcieveTsConstants() {
  for(int i = 0; i < 2; i++) {
    for(int j = 0; j < 3; j++) {
      byte *tsConstant = (byte*)(&(tsConstants[i][j]));
      for(int k = 0; k < 8; k++) {
        tsConstant[k] = Serial.read();
      }
    }
  }
}

int lerp(int a, int b, float value){
  return a+((b-a)*value);
}




