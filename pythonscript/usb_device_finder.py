import usb.core
import usb.util

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

# Print all interfaces and their endpoints
for i in range(cfg.bNumInterfaces):
    interface = usb.util.find_descriptor(cfg, bInterfaceNumber=i)
    print(f"Interface {i}:")
    for endpoint in interface:
        print(f"  Endpoint Address: {endpoint.bEndpointAddress}")
        print(f"  Attributes: {endpoint.bmAttributes}")
        print(f"  Max Packet Size: {endpoint.wMaxPacketSize}")

# Release the device
usb.util.dispose_resources(dev)
