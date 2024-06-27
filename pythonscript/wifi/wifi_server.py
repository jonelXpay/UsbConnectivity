import socket
import json
import threading

# Function to send data to Android
def send_data():
    data_to_send = {
        'transactionId': '4b546514-fb54-47a2-96f7-6a465c3b3422',
        'posId': '4b546514-fb54-47a2-96f7-6a465c3b3422',
        'timestamp': '8/9/2024 12:00:00',
        'transactionType': 'Purchase',
        'subTotalAmount': '120.00',
        'totalAmount': '120.00',
        'products': [
            {
                'productName': 'Shampoo Mo',
                'brand': 'Shampoo Ko',
                'quantity': '69',
                'unit': 'pcs',
                'pricePerUnit': '42.00',
                'currency': 'php',
                'sku': 'SDF154SD51212ADSD848'
            }
        ],
        'callbackUrl': 'https://your-host-api/api-name'
    }
    data_json = json.dumps(data_to_send)

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            client_socket.connect(('192.168.1.11', 5001))  # Replace with the IP of your Android device
            client_socket.sendall(data_json.encode('utf-8'))
            print(f"Data sent to Android: {data_json}")
    except Exception as e:
        print(f"Error sending data: {e}")

# Function to handle each client connection
def handle_client_connection(client_socket, addr):
    try:
        print(f"Connection from {addr}")
        data = client_socket.recv(1024).decode('utf-8')
        received_data = json.loads(data)
        print(f"Received from Android: {received_data}")
    except Exception as e:
        print(f"Error receiving data: {e}")
    finally:
        client_socket.close()

# Function to receive data from Android
def receive_data():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind(('0.0.0.0', 5002))  # Port for receiving data from Android
        server_socket.listen(5)
        print("Server started, waiting for connection...")

        while True:
            client_socket, addr = server_socket.accept()
            threading.Thread(target=handle_client_connection, args=(client_socket, addr)).start()

if __name__ == "__main__":
    # Start the receive function in a separate thread
    threading.Thread(target=receive_data, daemon=True).start()

    while True:
        input("Press Enter to send data...")
        send_data()
