

#include <ESP8266WiFi.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>

/* Set these to your desired credentials. */
const char *ssid = "DCmote";
const char *password = "riofbicid";

ESP8266WebServer server(80);

/* Just a little test message.  Go to http://192.168.4.1 in a web browser
 * connected to this access point to see it.
 */
void handleRoot() {
	server.send(200, "text/html", "<h1>You are connected</h1>");
}

void lon() {
 
   
  server.send(200, "text/plain", "LED is keep on");
}

void loff() {
  
   
  server.send(200, "text/plain", "LED is keep Off");
}

void setup() {
	delay(1000);
	Serial.begin(115200);
	Serial.println();
	Serial.print("Configuring access point...");
	/* You can remove the password parameter if you want the AP to be open. */
	WiFi.softAP(ssid, password);

	IPAddress myIP = WiFi.softAPIP();
	Serial.print("AP IP address: ");
	Serial.println(myIP);
	server.on("/", handleRoot);
    server.on("/off", lon);  // this is a commad 
       server.on("/on", loff); // this is a commad 
	server.begin();
	Serial.println("HTTP server started");
}

void loop() {
	server.handleClient();
}
