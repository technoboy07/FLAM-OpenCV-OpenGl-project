export class WebSocketClient {
    constructor(url = "ws://192.168.29.82:8080/ws") {
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        this.isConnected = false;
        this.url = url;
    }
    connect() {
        try {
            this.ws = new WebSocket(this.url);
            this.ws.onopen = () => {
                console.log('WebSocket connected');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this.onConnectionCallback?.(true);
            };
            this.ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                }
                catch (error) {
                    console.error('Failed to parse WebSocket message:', error);
                }
            };
            this.ws.onclose = () => {
                console.log('WebSocket disconnected');
                this.isConnected = false;
                this.onConnectionCallback?.(false);
                this.attemptReconnect();
            };
            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.onErrorCallback?.('WebSocket connection error');
            };
        }
        catch (error) {
            console.error('Failed to create WebSocket connection:', error);
            this.onErrorCallback?.('Failed to create WebSocket connection');
        }
    }
    handleMessage(message) {
        switch (message.type) {
            case 'frame':
                this.onFrameCallback?.(message.data);
                break;
            case 'stats':
                this.onStatsCallback?.(message.data);
                break;
            case 'error':
                this.onErrorCallback?.(message.data);
                break;
            default:
                console.warn('Unknown message type:', message.type);
        }
    }
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        }
        else {
            console.error('Max reconnection attempts reached');
            this.onErrorCallback?.('Connection lost. Max reconnection attempts reached.');
        }
    }
    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.isConnected = false;
    }
    send(message) {
        if (this.ws && this.isConnected) {
            this.ws.send(JSON.stringify(message));
        }
        else {
            console.warn('WebSocket not connected');
        }
    }
    // Event handlers
    onFrame(callback) {
        this.onFrameCallback = callback;
    }
    onStats(callback) {
        this.onStatsCallback = callback;
    }
    onError(callback) {
        this.onErrorCallback = callback;
    }
    onConnection(callback) {
        this.onConnectionCallback = callback;
    }
    isWebSocketConnected() {
        return this.isConnected && this.ws?.readyState === WebSocket.OPEN;
    }
}
//# sourceMappingURL=WebSocketClient.js.map