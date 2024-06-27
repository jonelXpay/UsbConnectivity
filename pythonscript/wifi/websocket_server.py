import asyncio
import websockets
import json

async def send_data(websocket):
    try:
        while True:
            # Prepare the payload
            payload = json.dumps({"message": "Hello from Python client!"})

            # Send the payload to the WebSocket server
            await websocket.send(payload)
            print(f"Sent: {payload}")

            # Wait for a short period before sending the next message
            await asyncio.sleep(10)  # Adjust the interval as needed
    except websockets.ConnectionClosed:
        print("Connection closed. Stopping sending data.")
    except Exception as e:
        print(f"Error in send_data: {e}")

async def receive_data(websocket):
    try:
        while True:
            # Receive a message from the WebSocket server
            response = await websocket.recv()
            print(f"Received: {response}")
    except websockets.ConnectionClosed:
        print("Connection closed. Stopping receiving data.")
    except Exception as e:
        print(f"Error in receive_data: {e}")

async def main():
    uri = "wss://7f77z033l6.execute-api.ap-southeast-1.amazonaws.com/development"
    async with websockets.connect(uri) as websocket:
        # Start sending and receiving data concurrently
        send_task = asyncio.create_task(send_data(websocket))
        receive_task = asyncio.create_task(receive_data(websocket))

        # Wait for both tasks to complete
        await asyncio.gather(send_task, receive_task)

if __name__ == "__main__":
    asyncio.run(main())
