// Rio Technical Solution 
// No 61 new economic center nidahasmawatha kegalle 
// 071 537 67 85 
// tirashana@gmail.com



#define BaudRate 9600

char incomingOption;

void setup()
{
  pinMode(13, OUTPUT);
  pinMode(12, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(8, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(4, OUTPUT);
  // serial communication
  Serial.begin(BaudRate);
}
void loop()
{
     //read from serial port getting information from VS 2013
     incomingOption = Serial.read();
     //verify incomingOption
     switch(incomingOption){
        case '0':
       
          digitalWrite(13, HIGH); ////// s1
          break;
        case '1':
        
          digitalWrite(13, LOW); ////// s1
          break;
           case '2':
        
          digitalWrite(12, HIGH); ////// s2
          break;
        case '3':
       
          digitalWrite(12, LOW);////// s2
           break;  
             case '4':
         
          digitalWrite(11, HIGH); ////// s3
          break;
        case '5':
          // Turn OFF LED
          digitalWrite(11, LOW);////// s3
          break; 
            case '6':
         
          digitalWrite(10, HIGH);////// s4
          break;
        case '7':
       
          digitalWrite(10, LOW);////// s4
          break; 
            case '8':
        
          digitalWrite(9, HIGH);////// s5
          break;
        case '9':
       
          digitalWrite(9, LOW);////// s5
          break; 
            case 'a':
        
          digitalWrite(8, HIGH);////// s6
          break;
        case 'b':
        
          digitalWrite(8, LOW);////// s6
          break; 
            case 'c':
         
          digitalWrite(7, HIGH);////// s7
          break;
        case 'd':
       
          digitalWrite(7, LOW);////// s7
          break; 
           case 'e':
       
          digitalWrite(6, HIGH);////// s8
          break;
        case 'f':
       
          digitalWrite(6, LOW);////// s8
          break;  
           case 'g':
          
          digitalWrite(5, HIGH);////// s9
          break;
        case 'h':
       
          digitalWrite(5, LOW);////// s19
          break; 
            case 'i':
          
          digitalWrite(4, HIGH);////// s10
          break;
        case 'k':
         
          digitalWrite(4, LOW);////// s10
          break; 
          

          
     }
}
