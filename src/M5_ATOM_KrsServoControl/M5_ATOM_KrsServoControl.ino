//
//  @file KrsServo1.ino
//  @brief KrsServoSample1
//  @author Kondo Kagaku Co.,Ltd.
//  @date 2017/12/26
//
//  ID:0のサーボをポジション指定で動かす
//  範囲は、左5500 - 中央7500 - 右9500
//  0.5秒ごとに指定数値まで動く
//  ICSの通信にはHardwareSerialを使います。
//


#include <IcsHardSerialClass.h>
#include "M5Atom.h"

const byte EN_PIN = 19;
const byte RXPIN = 33;
const byte TXPIN = 23;
const long BAUDRATE = 1250000;
const int TIMEOUT = 1000;    //通信できてないか確認用にわざと遅めに設定

IcsHardSerialClass krs(&Serial2,EN_PIN,BAUDRATE,TIMEOUT);  //インスタンス＋ENピン(2番ピン)およびUARTの指定

int split(String data, char delimiter, String *dst){
    int index = 0;
    int arraySize = (sizeof(data)/sizeof((data)[0]));  
    int datalength = data.length();
    for (int i = 0; i < datalength; i++) {
        char tmp = data.charAt(i);
        if ( tmp == delimiter ) {
            index++;
            if ( index > (arraySize - 1)) return -1;
        }
        else dst[index] += tmp;
    }
    return (index + 1);
}
char buffer[33];

String getSerial(){
  int index = 0;
  bool hasData = false;
  String label = "";

  //入力された文字列の取得を試みる
  while (Serial.available() > 0) {
    hasData = true;
    buffer[index] = Serial.read();
    index++;
    //バッファ以上の場合は中断
    if (index >= 32) {
      break;
      //改行がある場合は改行を除いて読み込み
    } else if(buffer[index-1]=='\n'){
      index--;
      break;
    }
  }
  //終端文字を足す
  buffer[index] = '\0';

  //バッファがある場合は文字列を更新して、終端以降を除去
  if (hasData == true) {
    label = buffer;
    label.trim();
//    Serial.println(label);
  }
  return label;
}

String cmds[3] = {"\0"};
bool mothionMakeMode = false;
bool runFlg = false;
const int chSize = 8;
const int mothionArraySize = 200;
int mothionArray[chSize][mothionArraySize] = {};
int mothionTempArray[chSize][mothionArraySize] = {};
int mothionArrayPos[chSize]={0,0,0,0,0,0,0,0};
int mothionTempArrayPos[chSize]={0,0,0,0,0,0,0,0};

void initMothoinArray(){
  for (auto & row : mothionArray){
    for(auto & i : row){
      i = 9999;
    }
  }
  
  for (auto & pos : mothionArrayPos){
    pos = 0;
  }
  
  for (auto & row : mothionTempArray){
    for(auto & i : row){
      i = 9999;
    }
  }

  for (auto & pos : mothionTempArrayPos){
    pos = 0;
  }
}

void copyToMotionArray(){
  for(int i=0;i<8;i++){
    for(int j=0;j<mothionArraySize;j++){
      if(mothionTempArray[i][j]==9999) break;
      mothionArray[i][j]=mothionTempArray[i][j];
    }
  }
}

void parseServoControl(){
  
  cmds[0]=cmds[1]=cmds[2]="";
  split(getSerial(),' ',cmds);
  Serial.println(cmds[0]);
  if(mothionMakeMode==false){
    if(cmds[0]=="S0"){
        krs.setPos(cmds[1].toInt(),krs.degPos((float)cmds[2].toInt()));
    }else if(cmds[0]=="S50"){
        mothionMakeMode = true;
    }
  }else{
    if(cmds[0]=="S0"){
        mothionTempArray[cmds[1].toInt()][mothionTempArrayPos[cmds[1].toInt()]]=cmds[2].toInt();
        mothionTempArrayPos[cmds[1].toInt()]++;
    }else if(cmds[0]=="S51"){
        mothionMakeMode = false;
    }else if(cmds[0]=="S100"){
        runFlg = false;
    }else if(cmds[0]=="S200"){
        copyToMotionArray();
        runFlg=true;
    }else if(cmds[0]=="S300"){
        runFlg = false;
        initMothoinArray();
    }
    
  }
}

void getServoPos(int chAndPos[2]){
  while(true){
    cmds[0]=cmds[1]=cmds[2]="";
    split(getSerial(),' ',cmds);
//    Serial.println(cmds[0]);
    if(cmds[0].equals("S0")){;
      chAndPos[0] = cmds[1].toInt();
      chAndPos[1] = cmds[2].toInt();
      return;
    }
  }
}

int chAndPos[2];
void setup() {
  M5.begin(true, false, true);
  Serial.begin(115200); 
  while(!Serial) {};
  Serial.println("1> Start Serial:");
  
  krs.begin();  //サーボモータの通信初期設定

  
  getServoPos(chAndPos);
}

int countDelay150 = 0;
void loop() {
  while(true){
    parseServoControl();
    if(countDelay150%3==0){
      if(runFlg){
        for(int i = 0; i<chSize;i++){
          if(mothionArray[i][mothionArrayPos[chSize]]==9999)continue;
          krs.setPos(i,krs.degPos((float)mothionArray[i][mothionArrayPos[chSize]]));
          mothionArrayPos[chSize]++;
        }
      }
      countDelay150=1;
    }
    countDelay150++;
    delay(50);
  }
}
