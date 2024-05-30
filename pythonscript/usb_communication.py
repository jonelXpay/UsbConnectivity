import serial
import json
import time

# Replace with the correct serial port (e.g., COM3 on Windows or /dev/ttyUSB0 on Linux)
SERIAL_PORT = '/dev/tty.usbmodem143301'
BAUD_RATE = 9600


def send_json(ser, data):
    json_data = json.dumps(data)
    ser.write(json_data.encode('utf-8'))
    ser.write(b'\n')  # Send newline to indicate end of JSON data


def receive_json(ser):
    line = ser.readline().decode('utf-8').strip()
    return json.loads(line)


def main():
    ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
    time.sleep(2)  # Wait for connection to establish

    # Example: Send JSON data to Android
    data_to_send = {"message": "Hello from PC"}

    try:
        send_json(ser, data_to_send)
        print(f"Sent: {data_to_send}")
    except json.JSONDecodeError as e:
        print(f"JSON decode error: {e}")

    # Example: Receive JSON data from Android
    try:
        received_data = receive_json(ser)
        print(f"Received: {received_data}")
    except json.JSONDecodeError as e:
        print(f"JSON decode error: {e}")

    ser.close()


if __name__ == "__main__":
    main()
