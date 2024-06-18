import asyncio
import json
from bleak import BleakClient, BleakScanner

ADDRESS = "508435C5-CA49-192B-222F-13E57F2CF28D"  # Replace with your Android device's MAC address
CHARACTERISTIC_UUID_WRITE = "87654321-4321-6789-4321-0fedcba98788"  # Replace with your write characteristic UUID
CHARACTERISTIC_UUID_READ = "00002902-0000-1000-8000-00805f9b3444"  # Replace with your read characteristic UUID

async def find_device_by_address(address):
    devices = await BleakScanner.discover()
    print("Discovered devices:")
    for device in devices:
        print(f"Device: {device.name}, Address: {device.address}")
        if device.address == address:
            return device
    return None

async def send_json_data(client, characteristic_uuid, data):
    json_data = json.dumps(data).encode('utf-8')
    await client.write_gatt_char(characteristic_uuid, json_data)
    print("Data sent successfully")

async def read_json_data(client, characteristic_uuid):
    response = await client.read_gatt_char(characteristic_uuid)
    data = json.loads(response.decode('utf-8'))
    print("Data received:", data)
    return data

async def main():
    device = await find_device_by_address(ADDRESS)
    if not device:
        print(f"Device with address {ADDRESS} not found.")
        return

    async with BleakClient(device) as client:
        if await client.is_connected():
            print("Connected to the device")

            # Sending data
            data_to_send = {"key": "value", "another_key": 12345}
            await send_json_data(client, CHARACTERISTIC_UUID_WRITE, data_to_send)

            # Receiving data
            received_data = await read_json_data(client, CHARACTERISTIC_UUID_READ)
        else:
            print("Failed to connect to the device")

loop = asyncio.get_event_loop()
loop.run_until_complete(main())
