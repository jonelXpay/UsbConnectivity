import socket
import json

def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('192.168.1.9', 8080))  # Bind to all interfaces on port 8080
    server_socket.listen(1)
    print('Server is listening on port 8080...')

    while True:
        client_socket, addr = server_socket.accept()
        print(f'Connection from {addr}')

        data = client_socket.recv(1024).decode()
        print(f'Received data: {data}')

        # Assuming the data is JSON
        json_data = json.loads(data)
        print(f'Parsed JSON: {json_data}')

        # Send a response
        response = {'status': 'success', 'data_received': json_data}
        client_socket.send(json.dumps(response).encode())

        client_socket.close()

if __name__ == '__main__':
    start_server()
