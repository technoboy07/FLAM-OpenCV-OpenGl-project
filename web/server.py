#!/usr/bin/env python3
"""
Simple WebSocket server for testing the OpenCV OpenGL Web Viewer
This server simulates the Android app's WebSocket communication
"""

import asyncio
import websockets
import json
import time
import random
from typing import Set

# Global set to track connected clients
connected_clients: Set[websockets.WebSocketServerProtocol] = set()

# Simulated frame data
def generate_mock_frame_data():
    """Generate mock frame data for testing"""
    return {
        "type": "frame",
        "data": {
            "timestamp": int(time.time() * 1000),
            "width": 1920,
            "height": 1080,
            "fps": random.uniform(10, 30),
            "processingMode": random.randint(0, 3),
            "processingTime": random.uniform(5, 50)
        }
    }

def generate_mock_stats():
    """Generate mock performance stats"""
    return {
        "type": "stats",
        "data": {
            "averageFPS": random.uniform(15, 25),
            "maxFPS": random.uniform(25, 35),
            "minFPS": random.uniform(5, 15),
            "averageProcessingTime": random.uniform(10, 40),
            "totalFrames": random.randint(100, 1000),
            "uptime": random.randint(10000, 100000)
        }
    }

# Global data storage for real-time communication
latest_frame_data = None
latest_stats_data = None
android_client = None
web_clients = set()

async def handle_client(websocket, path):
    """Handle WebSocket client connections"""
    global android_client, latest_frame_data, latest_stats_data
    print(f"Client connected: {websocket.remote_address}")
    connected_clients.add(websocket)
    
    try:
        # Send welcome message
        welcome_message = {
            "type": "info",
            "data": "Connected to OpenCV OpenGL Web Viewer Server"
        }
        await websocket.send(json.dumps(welcome_message))

        client_type = "web"  # Default to web client

        # Send existing data to new client
        if latest_frame_data:
            await websocket.send(json.dumps(latest_frame_data))
        if latest_stats_data:
            await websocket.send(json.dumps(latest_stats_data))

        # Listen for messages from client
        while True:
            try:
                message = await websocket.recv()
                data = json.loads(message)

                if data.get("type") == "frame":
                    # Accept and forward frame data (including base64 image if present)
                    latest_frame_data = data
                    android_client = websocket
                    client_type = "android"

                    # Forward to all web clients (not the android client)
                    for client in list(connected_clients):
                        if client != websocket and client != android_client:
                            try:
                                await client.send(message)
                            except Exception as e:
                                print(f"Error sending frame to client: {e}")
                                connected_clients.discard(client)

                elif data.get("type") == "stats":
                    latest_stats_data = data
                    android_client = websocket
                    client_type = "android"

                    # Forward to all web clients (not the android client)
                    for client in list(connected_clients):
                        if client != websocket and client != android_client:
                            try:
                                await client.send(message)
                            except Exception as e:
                                print(f"Error sending stats to client: {e}")
                                connected_clients.discard(client)

                print(f"Received {data.get('type', 'unknown')} data from {client_type} client")

            except websockets.exceptions.ConnectionClosed:
                break
            except Exception as e:
                print(f"Error processing message from {websocket.remote_address}: {e}")
                break

    except websockets.exceptions.ConnectionClosed:
        print(f"Client disconnected: {websocket.remote_address}")
    except Exception as e:
        print(f"Error handling client {websocket.remote_address}: {e}")
    finally:
        connected_clients.discard(websocket)
        if websocket == android_client:
            android_client = None
            print("Android client disconnected")

async def main():
    """Start the WebSocket server"""
    print("Starting OpenCV OpenGL Web Viewer Server...")
    print("WebSocket server will run on ws://localhost:8080/ws")
    print("Web viewer available at http://localhost:8080")
    print("Press Ctrl+C to stop the server")
    
    # Start WebSocket server
    ws_server = await websockets.serve(handle_client, "0.0.0.0", 8080, subprotocols=["websocket"])
    
    # Start HTTP server for serving static files on port 8081
    import subprocess
    import os
    
    # Change to web directory and start HTTP server
    web_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(web_dir)
    
    # Start HTTP server in background on port 8081
    http_process = subprocess.Popen(["python3", "-m", "http.server", "8081"], 
                                  stdout=subprocess.DEVNULL, 
                                  stderr=subprocess.DEVNULL)
    
    print(f"HTTP server running on http://localhost:8081")
    print(f"WebSocket server running on ws://localhost:8080/ws")
    
    try:
        await ws_server.wait_closed()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        http_process.terminate()
        ws_server.close()
        await ws_server.wait_closed()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nServer stopped by user")
    except Exception as e:
        print(f"Server error: {e}")
