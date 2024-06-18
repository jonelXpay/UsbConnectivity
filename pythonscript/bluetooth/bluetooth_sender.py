import asyncio
import json
from bleak import BleakClient, BleakScanner

ADDRESS = "508435C5-CA49-192B-222F-13E57F2CF28D"  # Replace with your Android device's MAC address
CHARACTERISTIC_UUID = "87654321-4321-6789-4321-0fedcba98765"  # Replace with your characteristic UUID

async def find_device_by_address(address):
    devices = await BleakScanner.discover()
    print("Discovered devices:")
    for device in devices:
        print(f"Device: {device.name}, Address: {device.address}")
        if device.address == address:
            return device
    return None

async def send_json_data(address, characteristic_uuid, data):
    device = await find_device_by_address(address)
    if not device:
        print(f"Device with address {address} not found.")
        return

    async with BleakClient(device) as client:
        if await client.is_connected():
            json_data = json.dumps(data).encode('utf-8')
            await client.write_gatt_char(characteristic_uuid, json_data)
            print("Data sent successfully")
        else:
            print("Failed to connect to the device")

data = {
    "key": "value",
    "another_key": 12345
}

loop = asyncio.get_event_loop()
loop.run_until_complete(send_json_data(ADDRESS, CHARACTERISTIC_UUID, data))

