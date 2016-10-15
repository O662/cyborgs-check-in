/*
 * --------------------------------------------------------------------------------------------------------------------
 * Example sketch/program showing how to read data from a PICC to serial.
 * --------------------------------------------------------------------------------------------------------------------
 * This is a MFRC522 library example; for further details and other examples see: https://github.com/miguelbalboa/rfid
 * 
 * Example sketch/program showing how to read data from a PICC (that is: a RFID Tag or Card) using a MFRC522 based RFID
 * Reader on the Arduino SPI interface.
 * 
 * When the Arduino and the MFRC522 module are connected (see the pin layout below), load this sketch into Arduino IDE
 * then verify/compile and upload it. To see the output: use Tools, Serial Monitor of the IDE (hit Ctrl+Shft+M). When
 * you present a PICC (that is: a RFID Tag or Card) at reading distance of the MFRC522 Reader/PCD, the serial output
 * will show the ID/UID, type and any data blocks it can read. Note: you may see "Timeout in communication" messages
 * when removing the PICC from reading distance too early.
 * 
 * If your reader supports it, this sketch/program will read all the PICCs presented (that is: multiple tag reading).
 * So if you stack two or more PICCs on top of each other and present them to the reader, it will first output all
 * details of the first and then the next PICC. Note that this may take some time as all data blocks are dumped, so
 * keep the PICCs at reading distance until complete.
 * 
 * @license Released into the public domain.
 * 
 * Typical pin layout used:
 * -----------------------------------------------------------------------------------------
 *             MFRC522      Arduino       Arduino   Arduino    Arduino          Arduino
 *             Reader/PCD   Uno/101       Mega      Nano v3    Leonardo/Micro   Pro Micro
 * Signal      Pin          Pin           Pin       Pin        Pin              Pin
 * -----------------------------------------------------------------------------------------
 * RST/Reset   RST          9             5         D9         RESET/ICSP-5     RST
 * SPI SS      SDA(SS)      10            53        D10        10               10
 * SPI MOSI    MOSI         11 / ICSP-4   51        D11        ICSP-4           16
 * SPI MISO    MISO         12 / ICSP-1   50        D12        ICSP-1           14
 * SPI SCK     SCK          13 / ICSP-3   52        D13        ICSP-3           15
 */

#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <SPI.h>
#include <MFRC522.h>

//#define SDA_PIN         4;         // A4
//#define SCL_PIN         5;         // A5
#define RST_PIN         9          // Configurable, see typical pin layout above
#define SS_PIN          10         // Configurable, see typical pin layout above

//void dumpToSerial(Uid *uid);

MFRC522 mfrc522(SS_PIN, RST_PIN);  // Create MFRC522 instance

/**********************************************************/
char array1[]=" SunFounder               ";  //the string to print on the LCD
char array2[]="hello, world!             ";  //the string to print on the LCD
int tim = 500;  //the value of delay time
// initialize the library with the numbers of the interface pins
LiquidCrystal_I2C lcd(0x27,16,2);  // set the LCD address to 0x27 for a 16 chars and 2 line display
unsigned long initialTime = millis();
/*********************************************************/

void setup() {
	Serial.begin(9600);		// Initialize serial communications with the PC
	while (!Serial);		// Do nothing if no serial port is opened (added for Arduinos based on ATMEGA32U4)
	SPI.begin();			// Init SPI bus
	mfrc522.PCD_Init();		// Init MFRC522
	mfrc522.PCD_DumpVersionToSerial();	// Show details of PCD - MFRC522 Card Reader details
	Serial.println(F("Scan PICC to see UID, SAK, type, and data blocks..."));
  lcd.init();       // initialize the lcd
  lcd.backlight();  // open the backlight
  lcd.print("Scan card");
}

void loop() {
	// Look for new cards
	if ( ! mfrc522.PICC_IsNewCardPresent()) {
		return;
	}

	// Select one of the cards
	if ( ! mfrc522.PICC_ReadCardSerial()) {
		return;
	}

	// Dump debug info about the card; PICC_HaltA() is automatically called
	//mfrc522.PICC_DumpToSerial(&(mfrc522.uid));
  //dumpToSerial(&(mfrc522.uid));
  dumpToSerial();
  mfrc522.PICC_HaltA();

  //lcd.backlight();
  //printMessage();
  printMessage2();
  //lcd.backlight();
  initialTime = millis();
}

/*********************************************************/
void printMessage() {
  lcd.setCursor(15,0);  // set the cursor to column 15, line 0
  for (int positionCounter1 = 0; positionCounter1 < 26; positionCounter1++)
  {
    lcd.scrollDisplayLeft();  //Scrolls the contents of the display one space to the left.
    lcd.print(array1[positionCounter1]);  // Print a message to the LCD.
    delay(tim);  //wait for 250 microseconds
  }
  lcd.clear();  //Clears the LCD screen and positions the cursor in the upper-left corner.
  lcd.setCursor(15,1);  // set the cursor to column 15, line 1
  for (int positionCounter = 0; positionCounter < 26; positionCounter++)
  {
    lcd.scrollDisplayLeft();  //Scrolls the contents of the display one space to the left.
    lcd.print(array2[positionCounter]);  // Print a message to the LCD.
    delay(tim);  //wait for 250 microseconds
  }
  lcd.clear();  //Clears the LCD screen and positions the cursor in the upper-left corner.
}
/************************************************************/

/************************************************************/
void printMessage2() {
  //lcd.display();
  lcd.clear();
  lcd.print("ID:");
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    if (mfrc522.uid.uidByte[i] < 0x10) {
      lcd.print(F(" 0"));
    } else {
      lcd.print(F(" "));
    }
    lcd.print(mfrc522.uid.uidByte[i], HEX);
  }
  lcd.setCursor(0, 1);
  lcd.print("Welcome");
  delay(5000);
  //lcd.noDisplay();
  lcd.clear();
  lcd.print("Scan card");
}
/************************************************************/

/************************************************************/
// After MFRC522.PICC_DumpToSerial(Uid *uid)
void dumpToSerial() {
  // Dump UID, SAK and Type 
  Serial.print(F("Card UID:"));
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    if (mfrc522.uid.uidByte[i] < 0x10) {
      Serial.print(F(" 0"));
    } else {
      Serial.print(F(" "));
    }
    Serial.print(mfrc522.uid.uidByte[i], HEX);
  }
  Serial.println();
}
/************************************************************/

