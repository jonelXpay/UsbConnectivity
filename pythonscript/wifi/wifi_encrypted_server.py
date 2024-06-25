import ssl
import socket
import json
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend
import base64

def generate_key_iv():
    key = os.urandom(32)  # AES-256 key size is 32 bytes
    iv = os.urandom(16)   # AES block size is 16 bytes
    return key, iv

def encrypt_text(plain_text, key, iv):
    plain_text_bytes = plain_text.encode('utf-8')
    key_bytes = key.encode('utf-8')
    iv_bytes = iv.encode('utf-8')

    padder = padding.PKCS7(algorithms.AES.block_size).padder()
    padded_data = padder.update(plain_text_bytes) + padder.finalize()

    cipher = Cipher(algorithms.AES(key_bytes), modes.CBC(iv_bytes), backend=default_backend())
    encryptor = cipher.encryptor()
    encrypted_text = encryptor.update(padded_data) + encryptor.finalize()

    return base64.b64encode(iv_bytes + encrypted_text).decode('utf-8')


def decrypt_text(encrypted_text, key, iv):
    encrypted_text_bytes = base64.b64decode(encrypted_text)
    key_bytes = base64.b64decode(key)
    iv_bytes = base64.b64decode(iv)

    cipher = Cipher(algorithms.AES(key_bytes), modes.CBC(iv_bytes), backend=default_backend())
    decryptor = cipher.decryptor()
    decrypted_padded_plaintext = decryptor.update(encrypted_text_bytes[16:]) + decryptor.finalize()

    unpadder = padding.PKCS7(algorithms.AES.block_size).unpadder()
    decrypted_plaintext = unpadder.update(decrypted_padded_plaintext) + unpadder.finalize()

    return decrypted_plaintext.decode('utf-8')

def start_server():

    # Correctly specify the paths to the certificate and key files
    certfile = '/Users/Jonel/AndroidStudioProjects/UsbConnectivity/pythonscript/sslcertificate/certificate.pem'
    keyfile = '/Users/Jonel/AndroidStudioProjects/UsbConnectivity/pythonscript/sslcertificate/private_key.pem'

    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=certfile, keyfile=keyfile)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0) as sock:
        sock.bind(('192.168.1.8', 44896))
        sock.listen(5)
        print("Server listening on port 8443...")

        with context.wrap_socket(sock, server_side=True) as ssock:
            conn, addr = ssock.accept()
            print(f"Connection from {addr}")
            data = conn.recv(1024).decode()
            print(f'Received data: {data}')

    # Generate a key and IV for AES encryption (for example purposes, normally you'd use a consistent key and IV)
            key = "BNl4j7220grWQmf0KmtU/wJtJOsoe9uP6ELt7k8q12M="  # 32 bytes for AES-256
            iv = "EMD6Pw5bmAmYflFczjCG9g=="  # 16 bytes for AES

             # Decrypt encrypted data
            decrypted_data = decrypt_text(data, key, iv)
            print("Decrypted data:", decrypted_data)

            json_data = json.loads(decrypted_data)
            print("JSON Data:", json_data)

            # Send a response
            response = {"status": "success", "data_received": json_data}
            print(f"JSON data to be sent: {response}")
            encrypt_response = encrypt_text(json.dumps(response), key, iv)
            print("encrypt_response: ", encrypt_response)
            conn.sendall(encrypt_response.encode('utf-8'))

            conn.close()

if __name__ == "__main__":
    start_server()
