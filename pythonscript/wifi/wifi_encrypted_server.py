import ssl
import socket
import json
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import base64
import os
import secrets

def encrypt_json(json_data, key, iv):
    # Convert JSON data to bytes
    json_bytes = json.dumps(json_data).encode('utf-8')

    # Initialize AES cipher in CBC mode with PKCS#5 padding
    cipher = AES.new(key, AES.MODE_CBC, iv)

    # Pad the data to match the block size of AES
    padded_data = pad(json_bytes, AES.block_size)

    # Encrypt the padded data
    encrypted_data = cipher.encrypt(padded_data)

    # Base64 encode the encrypted data for easier storage/transmission
    return base64.b64encode(encrypted_data).decode('utf-8')

def decrypt_json(encrypted_data, key, iv):
    # Base64 decode the encrypted data
    encrypted_data = base64.b64decode(encrypted_data)

    # Initialize AES cipher in CBC mode with PKCS#5 padding
    cipher = AES.new(key, AES.MODE_CBC, iv)

    # Decrypt the data
    decrypted_data = cipher.decrypt(encrypted_data)

    # Unpad the decrypted data
    unpadded_data = unpad(decrypted_data, AES.block_size)

    # Convert bytes to JSON string and return
    return json.loads(unpadded_data.decode('utf-8'))

def decrypt_text(encrypted_text, key):
    # Decode base64-encoded string and separate IV and ciphertext
    ciphertext_with_iv = base64.b64decode(encrypted_text)
    iv = ciphertext_with_iv[:AES.block_size]
    ciphertext = ciphertext_with_iv[AES.block_size:]

    # Initialize AES cipher in CBC mode with PKCS#5 padding
    cipher = AES.new(key, AES.MODE_CBC, iv)

    # Decrypt the ciphertext
    decrypted_padded_plaintext = cipher.decrypt(ciphertext)

    # Unpad the decrypted padded plaintext and decode to text
    plaintext = unpad(decrypted_padded_plaintext, AES.block_size).decode('utf-8')

    return plaintext

# Define the encryption key (32 bytes for AES-256)
key = secrets.token_bytes(32)  # Replace 'Sixteen byte key' with your 32-byte encryption key

# Define the initialization vector (IV) (16 bytes for AES)
iv = os.urandom(16)   # Replace 'Sixteen byte iv' with your 16-byte IV

def start_server():

    # Correctly specify the paths to the certificate and key files
    certfile = '/Users/Jonel/AndroidStudioProjects/UsbConnectivity/pythonscript/sslcertificate/certificate.pem'
    keyfile = '/Users/Jonel/AndroidStudioProjects/UsbConnectivity/pythonscript/sslcertificate/private_key.pem'

    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=certfile, keyfile=keyfile)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0) as sock:
        sock.bind(('192.168.1.8', 43394))
        sock.listen(5)
        print("Server listening on port 8443...")

        with context.wrap_socket(sock, server_side=True) as ssock:
            conn, addr = ssock.accept()
            print(f"Connection from {addr}")
            data = conn.recv(1024).decode()
            print(f'Received data: {data}')

             # Decrypt encrypted data
            decrypted_data = decrypt_text(data, key, iv)
            print("Decrypted data:", decrypted_data)

            json_data = json.loads(data.decode('utf-8'))
            print(f"Received JSON data: {json_data}")

            # Send a response
            response = {'status': 'success', 'data_received': json_data}
            conn.send(json.dumps(response).encode())

            conn.close()

if __name__ == "__main__":
    start_server()
