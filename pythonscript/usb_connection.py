import usb.core
import usb.util
import time

# Replace these with your actual vendor and product IDs
VENDOR_ID = 0x04e8
PRODUCT_ID = 0x6860

# Find the device
dev = usb.core.find(idVendor=VENDOR_ID, idProduct=PRODUCT_ID)

if dev is None:
    raise ValueError("Device not found")

# Detach kernel driver if necessary
if dev.is_kernel_driver_active(0):
    dev.detach_kernel_driver(0)

dev.set_configuration()
cfg = dev.get_active_configuration()

# Use the correct interface number (0 in this example)
intf = cfg[(0, 0)]

# Find endpoints
ep_in = usb.util.find_descriptor(
    intf,
    custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_IN
)
ep_out = usb.util.find_descriptor(
    intf,
    custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_OUT
)

assert ep_in is not None, "IN endpoint not found"
assert ep_out is not None, "OUT endpoint not found"

# Send JSON data to Android
json_data = '{"message": "Hello from PC"}'
ep_out.write(json_data.encode('utf-8'))
print("Sent JSON data to Android")

# Receive JSON data from Android
time.sleep(1)  # Wait for Android to send data back
received_data = ep_in.read(1024)
print("Received JSON data from Android: ", received_data.tobytes().decode('utf-8'))

# Release the device
usb.util.release_interface(dev, intf)
dev.attach_kernel_driver(0)
