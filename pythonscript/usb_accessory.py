import serial
import json

def main():
    # Replace '/dev/tty.usbmodem' with the actual serial port on your MacBook
    ser = serial.Serial('/dev/tty.usbmodem14201', 9600)

    # Read JSON data from Android device
    data = ser.readline().decode('utf-8').strip()

    # Deserialize JSON data
    json_data = json.loads(data)

    # Process JSON data
    response = {'message': 'Data received successfully'}

    # Serialize response to JSON
    response_json = json.dumps(response)

    # Send response back to Android device
    ser.write(response_json.encode('utf-8'))

    # Close serial connection
    ser.close()

if __name__ == "__main__":
    main()