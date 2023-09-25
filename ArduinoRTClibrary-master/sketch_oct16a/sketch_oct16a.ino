#include <virtuabotixRTC.h>
#include <SoftwareSerial.h> // 引用程式庫
SoftwareSerial BT(0,1); // 0,1接藍芽TX,RX
//接收藍芽傳的文字
String tx;

// DS1302接線指示: 可依需求修改
// DS1302 CLL/SCLK  --> 10
// DS1302 DAT/IO --> 9
// DS1302 RST/CE --> 8
// DS1302 VCC --> 3.3v -5v
// DS1302 GND --> GND

virtuabotixRTC myRTC(10,9,8);
const int sw = 6;
const int bee = 7;
//const int led = 13;

const int morning_led = 11;
const int afternoon_led = 12;
const int evening_led = 13;

const int debounceDelay=1000;

const int MORNING = 1;
const int AFTERNOON = 2;
const int EVENING = 3;

// 設早中晚鬧鐘

// 早上鬧鐘
bool have_set_alarm_morning = false;
int hours_morning = 0;
int minutes_morning = 0;

// 中午鬧鐘
bool have_set_alarm_afternoon = true;
int hours_afternoon = 16;
int minutes_afternoon = 56;

// 晚上鬧鐘
bool have_set_alarm_evening = false;
int hours_evening = 0;
int minutes_evening = 0;

void setup() {
  pinMode(sw,INPUT);
  digitalWrite(sw,HIGH);
  //pinMode(led,OUTPUT);
  
  pinMode(morning_led,OUTPUT);
  pinMode(afternoon_led,OUTPUT);
  pinMode(evening_led,OUTPUT);
  
  pinMode(bee,OUTPUT);
  Serial.begin(38400);
  BT.begin(38400);
  myRTC.setDS1302Time(00, 40, 18, 1, 28, 11, 2022);
}

void loop() {

  // 確認藍芽有無傳送訊息
  if(BT.available()){
    tx = BT.readString();
    Serial.print(tx);
    getTime(tx);
  }
  
  myRTC.updateTime();

  // 確認早、中、晚鬧鐘有無設定
  if(have_set_alarm_morning || have_set_alarm_afternoon || have_set_alarm_evening)
  {
    // 確認早上鬧鐘有無設定
    if(have_set_alarm_morning && myRTC.hours == hours_morning && myRTC.minutes == minutes_morning && myRTC.seconds >= 0 && myRTC.seconds <= 2)
    {
      digitalWrite(bee,HIGH);
      digitalWrite(morning_led,HIGH);

      while(true)
      {
        int val = digitalRead(sw);
        if(val == LOW)
        {
          delay(debounceDelay);
          while(digitalRead(sw) == LOW);
          digitalWrite(bee,LOW);
          digitalWrite(morning_led,LOW);
          break;
        }
      }
    }
    // 確認中午鬧鐘有無設定
    if(have_set_alarm_afternoon && myRTC.hours == hours_afternoon && myRTC.minutes == minutes_afternoon && myRTC.seconds >= 0 && myRTC.seconds <= 2)
    {
      digitalWrite(bee,HIGH);
      digitalWrite(afternoon_led,HIGH);
      
      while(true)
      {
        int val = digitalRead(sw);
        if(val == LOW)
        {
          delay(debounceDelay);
          while(digitalRead(sw) == LOW);
          digitalWrite(bee,LOW);
          digitalWrite(afternoon_led,LOW);
          break;
        }
      }
    }
    // 確認晚上鬧鐘有無設定
    if(have_set_alarm_evening && myRTC.hours == hours_evening && myRTC.minutes == minutes_evening && myRTC.seconds >= 0 && myRTC.seconds <= 2)
    {
      digitalWrite(bee,HIGH);
      digitalWrite(evening_led,HIGH);

      while(true)
      {
        int val = digitalRead(sw);
        if(val == LOW)
        {
          delay(debounceDelay);
          while(digitalRead(sw) == LOW);
          digitalWrite(bee,LOW);
          digitalWrite(evening_led,LOW);
          break;
        }
      }
    }
  }
  
  showTime();
}

void showTime()
{
  Serial.print("Current Date / Time : ");
  Serial.print(myRTC.dayofmonth);
  Serial.print("/");
  Serial.print(myRTC.month);
  Serial.print("/");
  Serial.print(myRTC.year);
  Serial.print("/");
  Serial.print(myRTC.hours);
  Serial.print("/");
  Serial.print(myRTC.minutes);
  Serial.print(":");
  Serial.println(myRTC.seconds);

  delay(1000);
  
}

void getTime(String str){
  
  // 確認輸入格式是否正確
  int hour_char_position = str.indexOf(':');
  String character = str.substring(0,1);
  int timePeriod = MORNING;
  int str_length;
  int hour;
  int minute;
  if(hour_char_position == -1 && (character != "M" && character != "A" || character != "E")){
     
  }
  else{

    if(character == "M")
      timePeriod = MORNING;
    else if(character == "A")
      timePeriod = AFTERNOON;
    else
      timePeriod = EVENING;
    switch(timePeriod){
        case MORNING:
          str_length = str.length();
          hour = str.substring(1,hour_char_position).toInt();
          minute = str.substring(hour_char_position + 1, str_length).toInt();
          // 確認小時、分鐘數字格式是否正確
          if(hour >=0 && hour <= 23 && minute >= 0 && minute <= 59)
          {
            hours_morning = hour;
            minutes_morning = minute;
            have_set_alarm_morning = true;
          }
          Serial.println("早上");
          Serial.print(hours_morning);
          Serial.print(":");
          Serial.println(minutes_morning);
          break;
        case AFTERNOON:
          str_length = str.length();
          hour = str.substring(1,hour_char_position).toInt();
          minute = str.substring(hour_char_position + 1, str_length).toInt();
          // 確認小時、分鐘數字格式是否正確
          if(hour >=0 && hour <= 23 && minute >= 0 && minute <= 59)
          {
            hours_afternoon = hour;
            minutes_afternoon = minute;
            have_set_alarm_afternoon = true;
          }
          Serial.println("中午");
          Serial.print(hours_afternoon);
          Serial.print(":");
          Serial.println(minutes_afternoon);
          break;
       case EVENING:
          str_length = str.length();
          hour = str.substring(1,hour_char_position).toInt();
          minute = str.substring(hour_char_position + 1, str_length).toInt();
          // 確認小時、分鐘數字格式是否正確
          if(hour >=0 && hour <= 23 && minute >= 0 && minute <= 59)
          {
            hours_evening = hour;
            minutes_evening = minute;
            have_set_alarm_evening = true;
          }
          Serial.println("晚上");
          Serial.print(hours_evening);
          Serial.print(":");
          Serial.println(minutes_evening);
          break;
    }
  }

  
}
