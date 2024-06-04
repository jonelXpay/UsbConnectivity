import usb.core
import usb.util
import json
import time

VENDOR_ID = 0x05c6
PRODUCT_ID = 0x9022

def find_device():
    dev = usb.core.find(idVendor=VENDOR_ID, idProduct=PRODUCT_ID)
    if dev is None:
        raise ValueError("Device not found")
    return dev

def setup_device(dev):
    dev.set_configuration(1)  # Set to your correct configuration number

    if dev.is_kernel_driver_active(0):
        dev.detach_kernel_driver(0)

    config = dev.get_active_configuration()
    interface = config[(0, 0)]  # Adjust to your correct interface number and alternate setting if needed

    ep_out = usb.util.find_descriptor(
        interface,
        custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_OUT
    )

    ep_in = usb.util.find_descriptor(
        interface,
        custom_match=lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_IN
    )

    if ep_out is None or ep_in is None:
        raise ValueError("Could not find required endpoints")

    return ep_out, ep_in

def send_json_data(ep_out, data):
    json_data = json.dumps(data).encode('utf-8')
    ep_out.write(json_data, timeout=5000)
    print('Data sent successfully')

def receive_json_data(ep_in):
    try:
        data = ep_in.read(1024, timeout=5000)
        json_data = data.tobytes().decode('utf-8')
        return json.loads(json_data)
    except usb.core.USBTimeoutError:
        print("Timeout error: Failed to receive data")
        return None

def main():
    try:
        dev = find_device()
        ep_out, ep_in = setup_device(dev)
        send_data = {"message": "Hello, Android"}
        send_json_data(ep_out, send_data)
        print("Sent data:", send_data)

        received_data = receive_json_data(ep_in)
        if received_data:
            print("Received data:", received_data)
    except usb.core.USBError as e:
        if e.errno == 19:  # No such device
            print("USB device disconnected, trying to reconnect...")
            time.sleep(1)  # Wait a bit before trying to reconnect
            try:
                dev = find_device()
                ep_out, ep_in = setup_device(dev)
                send_data = {"message": "Hello, Android"}
                send_json_data(ep_out, send_data)
                print("Sent data:", send_data)

                received_data = receive_json_data(ep_in)
                if received_data:
                    print("Received data:", received_data)
            except Exception as ex:
                print("Failed to reconnect to the device:", ex)
        else:
            print("USB error:", e)

if __name__ == "__main__":
    main()
