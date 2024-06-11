import asyncio
import json
from btlewrap.base import BluetoothInterface, PeripheralInterface
from btlewrap.bluepy import BluepyBackend

# Define service and characteristic UUIDs
SERVICE_UUID = "12345678-1234-5678-1234-56789abcdef0"
CHARACTERISTIC_UUID = "87654321-4321-6789-4321-fedcba987654"

class MyPeripheral:
    def __init__(self):
        self.backend = BluepyBackend
        self.interface = BluetoothInterface(backend=self.backend)
        self.peripheral = None
        self.json_data = {"message": "Hello from Python!"}

    async def start(self):
        # Find the device (replace with your device's MAC address)
        devices = await self.interface.discover(timeout=5)
        device = next((d for d in devices if d.name == "YOUR_DEVICE_NAME"), None)
        if not device:
            raise RuntimeError("Could not find your BLE device")

        # Connect to the device
        self.peripheral = BluepyPeripheral(device.address, self.backend)
        await self.peripheral.connect()

        # Set up the characteristic
        service = self.peripheral.get_service_by_uuid(SERVICE_UUID)
        characteristic = service.get_characteristic(CHARACTERISTIC_UUID)
        await characteristic.write(json.dumps(self.json_data).encode('utf-8'))

        print("BLE peripheral started. JSON data sent.")

    async def stop(self):
        if self.peripheral:
            await self.peripheral.disconnect()

async def main():
    peripheral = MyPeripheral()
    await peripheral.start()
    await asyncio.sleep(10)  # Keep the connection for a while
    await peripheral.stop()

if __name__ == "__main__":
    asyncio.run(main())