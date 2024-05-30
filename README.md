## USB Connection POC
A simple app for sending and receiving data between android device and PC using USB Port.

## Requirements
-   Android Studio Iguana | 2023.2.1 +
-   JDK version: 8 +
-   Kotlin : 1.9.0 +
-   Gradle version: 8.3.0
-   Kotlin KTS and Version Catalog

## Getting Started

1. Download Android studio.
2. Go to Terminal or Git on the Top Menu next to Tools
3. Clone this repository [https://github.com/jonelXpay/UsbConnectivity.git]
4. You will have a running version of the UsbConnectivity app
5. Try and Build. Happy Coding!

## USB Connection Details
To send and receive JSON data from a PC to an Android phone using a USB cable, we need to set up a communication protocol where the Android device acts as the USB host, and the PC acts as the USB accessory. This setup involves the following steps:
1. PC Side: Create a Python script to send and receive JSON data.
2. Android Side: app to handle USB communication.

## USB Communication Setup with Python as USB Accessory

1. Install Python in Mac or Linux
2. Go to Terminal to input this command [pip3 install pyserial]
3. In Project, access [usb_communication.py] where send and received json from USB Accessory. 
   * Inside python script, Replace with the correct serial port [SERIAL_PORT]
   * To retrieve the correct serial port run [ls /dev/tty.*] in the terminal
   * run [usb_communication.py] in terminal with [python3 usb_communication.py] command

## USB Communication Setup with Android App as USB Host

1. After successfully cloned the application
2. check the [device_filter.xml] under res>xml folder.
      * This how to declare the corresponding resource file that specifies the USB devices that we interested in
      * e.g [<usb-device vendor-id="1234" product-id="5678" />]
3. To replace and get device vendorID and ProductID in Mac
   * To find the vendor and product IDs of your USB serial device on a MacBook, you can use the system_profiler command in the Terminal.
   * Ensure your USB device (e.g. USB-to-Serial adapter) is connected to your MacBook.
   * run [system_profiler SPUSBDataType] in terminal and Look through the output for your USB device. The output will contain sections for each connected USB device, showing details such as the vendor ID and product ID.