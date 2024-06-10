import asyncio
from bleak import BleakClient, BleakScanner

SERVICE_UUID = "12345678-1234-5678-1234-56789abcdef0"
CHARACTERISTIC_UUID = "87654321-4321-6789-4321-0fedcba98765"

async def notification_handler(sender, data):
    print(f"Received data: {data.decode('utf-8')}")

async def main():
    devices = await BleakScanner.discover()
    target_device = None

    for device in devices:
        print(f"Found device: {device.name}, {device.address}")
        if device.name == "N950":  # Replace with your device name
            target_device = device
            break

    if target_device is None:
        print("Device not found")
        return

    async with BleakClient(target_device.address) as client:
        await client.start_notify(CHARACTERISTIC_UUID, notification_handler)
        print("Started notification")

        try:
            await asyncio.sleep(30)  # Keep the connection alive for 30 seconds
        except asyncio.CancelledError:
            pass

        await client.stop_notify(CHARACTERISTIC_UUID)
        print("Stopped notification")

if __name__ == "__main__":
    asyncio.run(main())