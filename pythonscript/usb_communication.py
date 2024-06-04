import usb.core
import usb.util
import json

# Find the USB device (replace with your own vendor and product ID)
dev = usb.core.find(idVendor=0x04e8, idProduct=0x6864)  # Example IDs for the Android host

if dev is None:
    raise ValueError("Device not found")

# Set the configuration
dev.set_configuration()

# Get an endpoint instance
cfg = dev.get_active_configuration()
intf = cfg[(0, 0)]

# Note: Replace these endpoint addresses with the correct ones for your device
ep_out = usb.util.find_descriptor(intf, custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_OUT)
ep_in = usb.util.find_descriptor(intf, custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_IN)

if ep_out is None or ep_in is None:
    raise ValueError("Could not find required endpoints")

# Send data
data = json.dumps({"key": "value"})
ep_out.write(data)

# Read data
response = ep_in.read(1024)
response_data = json.loads(response.tobytes().decode('utf-8'))
print("Received data:", response_data)
