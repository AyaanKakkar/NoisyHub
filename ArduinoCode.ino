#include <ESP8266WiFi.h>
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <WiFiManager.h>         // https://github.com/tzapu/WiFiManager

// Set web server port number to 80
WiFiServer server(80);

// Variable to store the HTTP request
String header;

// Auxiliar variables to store the current output state

// Assign output variables to GPIO pins

void setup() {
  pinMode(4,OUTPUT);
  pinMode(5,OUTPUT);
  
  pinMode(14,OUTPUT);
  pinMode(12,OUTPUT);
  
  digitalWrite(4,0);
  digitalWrite(5,0);
  digitalWrite(14,0);
  digitalWrite(12,0);
  Serial.begin(115200);
  
  // Initialize the output variables as outputs
  
  // Set outputs to LOW
  

  // WiFiManager
  // Local intialization. Once its business is done, there is no need to keep it around
  WiFiManager wifiManager;
  
  // Uncomment and run it once, if you want to erase all the stored information
  //wifiManager.resetSettings();
  
  // set custom ip for portal
  //wifiManager.setAPConfig(IPAddress(10,0,1,1), IPAddress(10,0,1,1), IPAddress(255,255,255,0));

  // fetches ssid and pass from eeprom and tries to connect
  // if it does not connect it starts an access point with the specified name
  // here  "AutoConnectAP"
  // and goes into a blocking loop awaiting configuration
  wifiManager.autoConnect("AutoConnectAP");
  // or use this for auto generated name ESP + ChipID
  //wifiManager.autoConnect();
  
  // if you get here you have connected to the WiFi
  Serial.println("Connected.");
  Serial.println(WiFi.localIP());
  Serial.println(WiFi.hostname());
  Serial.println(WiFi.status());
  if (!MDNS.begin("esp8266")) {
    Serial.println("Error setting up MDNS responder!");
    while (1) {
      delay(1000);
    }
  }
  Serial.println("mDNS responder started");

  server.begin();
  Serial.println("TCP server started");

  // Add service to MDNS-SD
  MDNS.addService("http", "tcp", 80);
}

void loop(){
  MDNS.update();
  WiFiClient client = server.available();   // Listen for incoming clients

  if (client) {                             // If a new client connects,
    
    Serial.println("New Client.");          // print a message out in the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      //Serial.println("AK IS CONNECTED");
      if (client.available()) {             // if there's bytes to read from the client,
        //char c = client.read(); 
        // read a byte, then
        //Serial.write(c);// print it out the serial monitor
        //if(c=='\n'){
          //Serial.println("");
        //}
        String t=client.readString();
        Serial.println("println :");
        Serial.println(t);
        
        int i = t.toInt();
        Serial.println(i);
        if(i==1){
          digitalWrite(4,1);
        }
        else if(i==2){
          digitalWrite(4,0);
        }
        else if(i==3){
          digitalWrite(5,1);
        }
        else if(i==4){
          digitalWrite(5,0);
        }
        else if(i==5){
          digitalWrite(14,1);
        }
        else if(i==6){
          digitalWrite(14,0);
        }
        else if(i==7){
          digitalWrite(12,1);
        }
        else if(i==8){
          digitalWrite(12,0);
          }
        
        
        //header += c;
      }
    }
    //header = "";
    
  // Wait for data from client to become available
//  while (client.connected() && !client.available()) {
//    delay(1);
//  }
//
//  // Read the first line of HTTP request
//  String req = client.readStringUntil('\r');
//  Serial.println(req);

//  client.flush();
  
//  Serial.println("Done with client");
//    // Close the connection
//    client.stop();
//    digitalWrite(4,0);
//    Serial.println("Client disconnected.");
//    Serial.println("");
  }
else{
  digitalWrite(4,0);
  //Serial.println("LED OFF");
}
}
