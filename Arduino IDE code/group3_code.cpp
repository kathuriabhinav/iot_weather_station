#include "DHT.h"
#include "MQ135.h"
#include <ESP8266WiFi.h>
#include <Arduino.h>
#include <Firebase_ESP_Client.h>
#include <SFE_BMP180.h>
#include <Wire.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

#define DHTTYPE DHT11

#define API_KEY "PUT YOUR API KEY HERE"     // Enter your Firebase API Key

#define DATABASE_URL "PUT YOUR DATABASE URL HERE"     // Enter your Firebase Database URL

FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;
int count = 0;
bool signupOK = false;

const char *ssid =  "NAME";     // Enter your WiFi Name
const char *pass =  "PASSWORD"; // Enter your WiFi Password

const char *weather_station_id =  "boath";     // Enter your Weather station ID (provided by u) 

const int sensorPin = 0;

int air_quality;

WiFiClient client;

#define dht_dpin 14

DHT dht(dht_dpin, DHTTYPE);


SFE_BMP180 bmp;
double phg = 0, pmb = 0;
#define ALTITUDE 98.0

void setup()
{
    dht.begin();
    Serial.begin(115200);
    Serial.println("Connecting to ");
    Serial.println(ssid);
    WiFi.begin(ssid, pass);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);
        Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    if (bmp.begin())
    {
        Serial.println("BMP180 init success");
    }
    else
    {
        Serial.println("BMP180 init fail\n\n");
    }

    delay(1000);

    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;
    if (Firebase.signUp(&config, &auth, "", ""))
    {
        Serial.println("firebase sign up ok");
        signupOK = true;
    }
    else
    {
        Serial.printf("%s\n", config.signer.signupError.message.c_str());
    }
    config.token_status_callback = tokenStatusCallback;
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);
}

void loop()
{

    float h = dht.readHumidity();
    float t = dht.readTemperature();

    MQ135 gasSensor = MQ135(A0);
    air_quality = gasSensor.getPPM();

    Serial.print("Humidity = ");
    Serial.print(h);
    Serial.println("%");

    Serial.print("Temperature = ");
    Serial.print(t);
    Serial.println("C");

    Serial.print("Air Quality = ");
    Serial.print(air_quality);
    Serial.println(" PPM.");

    char status;
    double T, P, p0, a;
    Serial.print("Altitude = ");
    Serial.print(ALTITUDE, 0);
    Serial.println(" meters");

    status = bmp.startTemperature();
    if (status != 0)
    {
        delay(status);
        status = bmp.getTemperature(T);
        if (status != 0)
        {
            status = bmp.startPressure(3);
            if (status != 0)
            {
                delay(status);
                status = bmp.getPressure(P, T);
                if (status != 0)
                {
                    pmb = P;
                    Serial.print("Absolute Pressure ");
                    Serial.print(P, 2);
                    Serial.print(" mb, ");
                    Serial.print(P * 0.0295333727, 2);
                    Serial.println(" inHg");
                    phg = P * 0.0295333727;

                    p0 = bmp.sealevel(P, ALTITUDE);
                }
                else 
                {
                    Serial.println("error retrieving pressure measurement\n");
                }
            }
            else 
            {
                Serial.println("error starting pressure measurement\n");
            }
        }
        else 
        {
            Serial.println("error retrieving temperature measurement\n");
        }
    }
    else 
    {
        Serial.println("error starting temperature measurement\n");
    }


    if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 15000 || sendDataPrevMillis == 0)) {
        sendDataPrevMillis = millis();

        if (Firebase.RTDB.setInt(&fbdo, "test/" + weather_station_id + "/temperature", t)) 
        {
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else 
        {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }

        if (Firebase.RTDB.setInt(&fbdo, "test/" + weather_station_id + "/humidity", h)) 
        {
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else 
        {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }

        if (Firebase.RTDB.setInt(&fbdo, "test/" + weather_station_id + "/air_quality", air_quality)) 
        {
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else 
        {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }


        if (Firebase.RTDB.setFloat(&fbdo, "test/" + weather_station_id + "/air_pressure", phg)) 
        {
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else
        {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }
    }
    delay(8000);
}