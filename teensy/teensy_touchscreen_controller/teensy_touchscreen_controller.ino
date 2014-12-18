#define LL 18 // lower left (with cable on bottom)
#define LR 15 // lower right
#define UL 20 // upper left
#define UR 14 // upper right

#define TOP A2 // top plate

int cmd;

long tStartTime;
int tStartX;
int tStartY;

int detectDelay = 200; // number of millis to wait while detecting touch type
int moveDistance = 40; // number of pixels that is a "move"

int state = 0;
// 0 -> not touched
// 1 -> detecting touch type
// 2 -> detecting if drag or right click
// 3 -> moving mouse
// 4 -> dragging mouse
// 5 -> configuring

long x = 0;
long y = 0;
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
  if(Serial.available()) {
    state = 5;
    cmd = Serial.read();
  }
  /*boolean clicked = pressed();
   if(clicked){
   readTS(state != 0);
   }*/
  boolean clicked = pressed();
  int tmpX = x;
  int tmpY = y;
  readTS(state);
  clicked = clicked && pressed();
  if(!clicked){
    x = tmpX;
    y = tmpY;
  }
  else{
    lastX = tmpX;
    lastY = tmpY;
  }
  //if(state != 0) Serial.println(state);
  switch(state){
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
    if(sq(x-tStartX)+sq(y-tStartY) >= sq(moveDistance)){
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
    if(sq(x-tStartX)+sq(y-tStartY) >= sq(moveDistance)){
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
    while(cmd != 0x00) {
      Serial.write(0x50); // waiting for command
      if(!Serial.available()) continue; // wait for input
      cmd = Serial.read();
      Serial.write(0x51); // ready
      if(cmd == 0x0f) { // read
        Serial.write(0x52); // about to read
        if(!pressed()) {
          Serial.write(0x53); // waiting for click
          while(!pressed()); // wait for click
          readTS(false);
          Serial.write(0x54); // recieved click
        }
        readTS(true);
        printXY();
        Serial.write(0x55); // done
      }
    }
    break;
  default:
    Serial.print("serious problem: state = ");
    Serial.println(state);
  }
}

void moveMouseToTouch(){
  //Serial.print("moving mouse to ");
  //printLoc();
  Serial.write(B10000000); // control "move"
  printXY();
}

void printXY() {
  Serial.write((x & 0x3F8) >> 3); // high 7 bits of x
  Serial.write(((x & 0x007) << 4) | ((y & 0x3C0) >> 6)); // low 3 bits of x, then high 4 bits of y
  Serial.write(y & 0x03F); // low 6 bits of y
}

void startLeftClick(){
  //Serial.println("starting left click");
  Serial.write(B11000000);
}

void endLeftClick(){
  //Serial.println("ending left click");
  Serial.write(B11000001);
}

void startRightClick(){
  //Serial.println("starting right click");
  Serial.write(B11000010);
}

void endRightClick(){
  //Serial.println("ending right click");
  Serial.write(B11000011);
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

int lerp(int a, int b, float value){
  return a+((b-a)*value);
}




