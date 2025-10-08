#!/usr/bin/env python3
"""
Simple WebSocket test server for debugging
"""

import asyncio
import websockets
import json

async def handle_client(websocket, path):
    """Handle WebSocket client connections"""
    print(f"Client connected: {websocket.remote_address}")
    
    try:
        # Send welcome message
        welcome_message = {
            "type": "info",
            "data": "Connected to test WebSocket server"
        }
        await websocket.send(json.dumps(welcome_message))
        print("Sent welcome message")
        
        # Listen for messages
        async for message in websocket:
            try:
                data = json.loads(message)
                print(f"Received: {data}")
                
                # Echo back
                response = {
                    "type": "echo",
                    "data": data
                }
                await websocket.send(json.dumps(response))
                print(f"Sent echo: {response}")
                
            except json.JSONDecodeError:
                print(f"Received non-JSON message: {message}")
                
    except websockets.exceptions.ConnectionClosed:
        print(f"Client disconnected: {websocket.remote_address}")
    except Exception as e:
        print(f"Error handling client {websocket.remote_address}: {e}")

async def main():
    """Start the test WebSocket server"""
    print("Starting test WebSocket server...")
    print("Server will run on ws://0.0.0.0:8080/ws")
    print("Press Ctrl+C to stop the server")
    
    # Start WebSocket server
    server = await websockets.serve(handle_client, "0.0.0.0", 8080)
    
    try:
        await server.wait_closed()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        server.close()
        await server.wait_closed()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nServer stopped by user")
    except Exception as e:
        print(f"Server error: {e}")
