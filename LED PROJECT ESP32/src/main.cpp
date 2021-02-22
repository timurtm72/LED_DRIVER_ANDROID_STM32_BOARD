#include <Arduino.h>
#include <Wire.h>
#include "TSl2581.h"
#include <esp32DHT.h>
#include <SPI.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include "esp32-hal-ledc.h"
#include "PietteTech_DHT.h" // Uncommend if building using CLI

//================================================================================================================
#define DHTTYPE DHT22 // Sensor type DHT11/21/22/AM2301/AM2302
#define DHTPIN 15     // Digital pin for communications

//declaration
void dht_wrapper(); // must be declared before the lib initialization

// Lib instantiate
PietteTech_DHT DHT(DHTPIN, DHTTYPE, dht_wrapper);
//================================================================================================================
TaskHandle_t TSL_task;
TaskHandle_t DHT_task;
TaskHandle_t wifi_task;
QueueHandle_t xQueueTH;
QueueHandle_t xQueueTSL;
SemaphoreHandle_t xSemaphore = NULL;
//================================================================================================================
WaveShare_TSL2581 tsl = WaveShare_TSL2581();
//DHT22 sensor;
int TSL2581_INT = 17;
//================================================================================================================
const char *ssid = "ESP32-Access-Point";
const char *password = "123456789";
StaticJsonDocument<500> doc;
Preferences preferences;
//================================================================================================================
// Set web server port number to 80
WiFiServer server(80);
WiFiClient client;
DeserializationError error;
//================================================================================================================
String readData = "";
String SendJsonData = "";
struct AMessage
{
  float temperatura;
  float humidity;
  char error_message[12];
} xMessageTH = {0, 0, " "};
float TSL_data = 0;
uint16_t pwmLevel = 0, inputPwmLevel = 0;
bool previousSwitchState = false;
//bool enableSave = false;
uint8_t switchData = 0;
//===============================================================================================================
void read_id(void)
{
  int id;
  int a;
  id = tsl.TSL2581_Read_ID();
  a = id & 0xf0;   //The lower four bits are the silicon version number
  if (!(a == 144)) //ID = 90H = 144D
  {
    Serial.println("false ");
  }
  else
  {
    Serial.print("I2C DEV is working ,id = ");
    Serial.println(id);
  }
}
//================================================================================================================
void Read_gpio_interrupt(uint16_t mindata, uint16_t maxdata)
{
  tsl.SET_Interrupt_Threshold(mindata, maxdata);
  int val = digitalRead(TSL2581_INT);
  if (val == 1)
  {
    //Serial.print("interrupt = 1 \n");
  }
  else
  {
    //Serial.print("interrupt = 0 \n");
    tsl.Reload_register();
  }
}
//================================================================================================================
void readDHT()
{
  //sensor.read();
}
//================================================================================================================
void TSL_task_func(void *parameter)
{
  unsigned long Lux;
  while (true)
  {
    //if (xSemaphoreTake(xSemaphore, (TickType_t)portMAX_DELAY))
    {
      tsl.TSL2581_Read_Channel();
      Lux = tsl.calculateLux(2, NOM_INTEG_CYCLE);
      Read_gpio_interrupt(2000, 50000);
      //Serial.print("Lux = ");
      //Serial.println(Lux);
      //Serial.print("TSL_task running on core ");
      //  "Блок loop() выполняется на ядре "
      //Serial.println(xPortGetCoreID());
      //xSemaphoreGive(xSemaphore);
      if (xQueueTSL != NULL)
      {
        // Отправить указатель на объект struct AMessage. Не блокируйте, если
        //очередь уже заполнена.
        TSL_data = Lux;
        xQueueSend(xQueueTSL, (void *)&TSL_data, portMAX_DELAY);
      }
    }
    vTaskDelay(pdMS_TO_TICKS(1500));
  }
}
//================================================================================================================
void DHT_task_func(void *parameter)
{
  while (true)
  {
    //if (xSemaphoreTake(xSemaphore, (TickType_t)portMAX_DELAY))
    {
      //  readDHT();
      //  sensor.onData([](float humidity, float temperatura) {
      //   xMessageTH.humidity = humidity;
      //   xMessageTH.temperatura = temperatura;
      //   Serial.printf("Temp: %gC\nHumid: %g%%\n",  xMessageTH.temperatura,  xMessageTH.humidity);
      // });
      // sensor.onError([](uint8_t error) {
      //   //Serial.printf("Sensor error: %s\n", sensor.getError());
      //   strcpy(xMessageTH.error_message, sensor.getError());
      // });
      int result = DHT.acquireAndWait(0);
#ifdef debug
      switch (result)
      {
      case DHTLIB_OK:
        Serial.println("Measuring DHT OK");
        break;
      case DHTLIB_ERROR_CHECKSUM:
        Serial.println("Error\n\r\tChecksum error");
        break;
      case DHTLIB_ERROR_ISR_TIMEOUT:
        Serial.println("Error\n\r\tISR time out error");
        break;
      case DHTLIB_ERROR_RESPONSE_TIMEOUT:
        Serial.println("Error\n\r\tResponse time out error");
        break;
      case DHTLIB_ERROR_DATA_TIMEOUT:
        Serial.println("Error\n\r\tData time out error");
        break;
      case DHTLIB_ERROR_ACQUIRING:
        Serial.println("Error\n\r\tAcquiring");
        break;
      case DHTLIB_ERROR_DELTA:
        Serial.println("Error\n\r\tDelta time to small");
        break;
      case DHTLIB_ERROR_NOTSTARTED:
        Serial.println("Error\n\r\tNot started");
        break;
      default:
        Serial.println("Unknown error");
        break;
      }
#endif
#ifdef debug
      Serial.print("Humidity (%): ");
#endif
      xMessageTH.humidity = DHT.getHumidity();
#ifdef debug
      Serial.println(xMessageTH.humidity, 2);
      Serial.print("Temperature (oC): ");
#endif
      xMessageTH.temperatura = DHT.getCelsius();
#ifdef debug
      Serial.println(xMessageTH.temperatura, 2);
#endif
      //Serial.print("DHT_task running on core ");
      //  "Блок loop() выполняется на ядре "
      //Serial.println(xPortGetCoreID());
      if (xQueueTH != NULL)
      {
        // Отправить указатель на объект struct AMessage. Не блокируйте, если
        //очередь уже заполнена.
        xQueueSend(xQueueTH, (void *)&xMessageTH, portMAX_DELAY);
      }
    }
    vTaskDelay(pdMS_TO_TICKS(2500));
  }
}
//================================================================================================================
const char WIFI_SSID[] = "HWEVA";     //"TP-LINK_EB86";
const char WIFI_PSWD[] = "timur1972"; //""renat2019";
//================================================================================================================
void readMeasuredData()
{
  if (xQueueTH != NULL)
  {
    if (xQueueReceive(xQueueTH, &xMessageTH, pdMS_TO_TICKS(1)) == pdPASS)
    {
      // Serial.printf("Temp: %.1fC\nHumid: %.1f%%\n", xMessageTH.temperatura, xMessageTH.humidity);
      //Serial.printf("Sensor status  %S\n", xMessageTH.error_message);
    }
  }

  if (xQueueTSL != NULL)
  {
    if (xQueueReceive(xQueueTSL, &(TSL_data), pdMS_TO_TICKS(1)) == pdPASS)
    {
      //Serial.printf("TSL value: %.1f Lux \n", TSL_data);
    }
  }
}
//============================================================================================================
void SavePwmData(uint16_t data)
{
  preferences.begin("data", false);
  preferences.putUShort("pwmLevel", data);
  preferences.end();
}
//============================================================================================================
uint16_t ReadPwmData()
{
  uint16_t tmp = 0;
  preferences.begin("data", false);
  tmp = (uint8_t)preferences.getUShort("pwmLevel");
  preferences.end();
  return tmp;
}
//============================================================================================================
//#define debug
void WritePwmToOut(uint32_t pwmLevel)
{
  uint32_t tmp = (uint32_t)(((uint32_t)pwmLevel * 255) / (uint32_t)100);
#ifdef debug
  Serial.print("WritePwmToOut: ");
  Serial.println(tmp);
#endif
  ledcWrite(0, (uint32_t)tmp);
}
//=============================================================================================================
#define UNKNOWN 0
#define CLICK_SWITCH_ON 1
#define CLICK_SWITCH_OFF 2
uint8_t PWM_LEVEL_STATE = UNKNOWN;
int status = WL_IDLE_STATUS;
//=============================================================================================================
void wifiTask_func(void *pvParam)
{
  //volatile static uint8_t inPwmLevel = 0;
  while (true)
  {
    WritePwmToOut(pwmLevel);
    if (status != WiFi.status())
    {
      // it has changed update the variable
      status = WiFi.status();

      if (status == WL_CONNECTED)
      {
// a device has connected to the AP
#ifdef debug
        Serial.println("Device connected to AP");
#endif
      }
      else
      {
// a device has disconnected from the AP, and we are back in listening mode
#ifdef debug
        Serial.println("Device disconnected from AP");
#endif
      }
    }
    client = server.available(); // listen for incoming clients
    //============================================================================================================
    if (client)
    {
#ifdef debug
      Serial.println("Client Connected");
#endif
      while (client.connected())
      {
        if (client.available())
        {
          char c = client.read();
          readData.concat(c);
          if ((readData.indexOf("\r\n") != -1) & (readData.indexOf("2020") != -1))
          {
            error = deserializeJson(doc, readData);
            if (error == OK)
            {
#ifdef debug
              Serial.print("Read data from client: ");
              Serial.println(readData);
#endif
              switchData = (doc["switch"].as<uint32_t>());
              inputPwmLevel = (uint16_t)doc["pwmlevel"].as<uint16_t>();
              switch (PWM_LEVEL_STATE)
              {
              case 0:
                if (switchData == 1)
                {
                  PWM_LEVEL_STATE = CLICK_SWITCH_ON;
                }
                break;
              case 1:
                if (switchData == 0)
                {
                  pwmLevel = inputPwmLevel;
                  PWM_LEVEL_STATE = CLICK_SWITCH_OFF;
                }
                else
                {
                  pwmLevel = inputPwmLevel;
                }
                break;
              case 2:
                SavePwmData(inputPwmLevel);
                pwmLevel = inputPwmLevel;
                PWM_LEVEL_STATE = UNKNOWN;
#ifdef debug
                Serial.println("Save data to flash...");
#endif
                break;
              }
              doc.clear();
              doc["temperatura"] = xMessageTH.temperatura;
              doc["humidity"] = xMessageTH.humidity;
              doc["light"] = TSL_data;
              if (switchData == 1)
              {
                doc["pwm"] = (uint16_t)inputPwmLevel;
              }
              else
              {
                doc["pwm"] = (uint16_t)pwmLevel;
              }
#ifdef debug
              Serial.print("pwmLevel: ");
              Serial.println(pwmLevel);
#endif
              // Generate the JSON string
              serializeJson(doc, SendJsonData);
              client.println(SendJsonData);
#ifdef debug
              Serial.print("Send data to client: ");
              Serial.println(SendJsonData);
#endif

              doc.clear();
            }
            else
            {
#ifdef debug
              Serial.println("Error recieve data from client...");
#endif
              //doc.clear();
              //previousSwitchState = false;
            }
            SendJsonData = "";
            readData = "";
          }
          readMeasuredData();
          WritePwmToOut(pwmLevel);
        }
      }
      //close the connection:
      client.stop();
#ifdef debug
      Serial.println("client disconnected");
#endif
    }
    //else
    //{
    // client = server.available(); // listen for incoming clients
    // }
    //================================================================================================================
  }
}
//================================================================================================================
void dht_wrapper()
{
  DHT.isrCallback();
}
//================================================================================================================

void setup(void)
{
  Serial.begin(115200);
  //sensor.setup(15);            // pin 2 is DATA, RMT channel defaults to channel 0 and 1
  Wire.begin(21, 22);          //i2c config
  pinMode(TSL2581_INT, INPUT); // sets the digital pin 7 as input
  read_id();
  /* Setup the sensor power on */
  tsl.TSL2581_power_on();
  //  /* Setup the sensor gain and integration time */
  tsl.TSL2581_config();
  pinMode(26, OUTPUT);
  ledcSetup(0, 250, 8); //0 - channel, 250 - HZ, 8 - rsolution
  ledcAttachPin(26, 0);
  pwmLevel = (uint16_t)ReadPwmData();
  Serial.print("Read data from flash... ");
  Serial.println(pwmLevel);
  //WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid, password);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(IP);
  server.begin(9876);
  xTaskCreatePinnedToCore(
      TSL_task_func, /* Функция, содержащая код задачи */
      "TSL_task",    /* Название задачи */
      4096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &TSL_task,     /* Идентификатор задачи */
      0);            /* Ядро, на котором будет выполняться задача */
  xTaskCreatePinnedToCore(
      DHT_task_func, /* Функция, содержащая код задачи */
      "DHT_task",    /* Название задачи */
      4096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &DHT_task,     /* Идентификатор задачи */
      0);            /* Ядро, на котором будет выполняться задача */
  xTaskCreatePinnedToCore(
      wifiTask_func, /* Функция, содержащая код задачи */
      "wifi_task",   /* Название задачи */
      4096,          /* Размер стека в словах */
      NULL,          /* Параметр создаваемой задачи */
      0,             /* Приоритет задачи */
      &wifi_task,    /* Идентификатор задачи */
      1);            /* Ядро, на котором будет выполняться задача */
  xQueueTSL = xQueueCreate(10, sizeof(TSL_data));
  xQueueTH = xQueueCreate(10, sizeof(xMessageTH));
  //xSemaphore = xSemaphoreCreateCounting(2, 0);
}
//================================================================================================================
void loop(void)
{
  vTaskDelete(NULL);
}
//================================================================================================================