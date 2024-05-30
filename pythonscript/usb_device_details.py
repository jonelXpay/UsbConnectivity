import usb.core
import usb.util

VENDOR_ID = 0x04e8
PRODUCT_ID = 0x6860

def find_device():
    dev = usb.core.find(idVendor=VENDOR_ID, idProduct=PRODUCT_ID)
    if dev is None:
        raise ValueError("Device not found")
    return dev

def print_device_details(dev):
    print("Device:", dev)
    for cfg in dev:
        print("Configuration:", cfg)
        for intf in cfg:
            print(" Interface:", intf)
            for ep in intf:
                print("  Endpoint:", ep)

def main():
    dev = find_device()
    print_device_details(dev)

if __name__ == "__main__":
    main()
