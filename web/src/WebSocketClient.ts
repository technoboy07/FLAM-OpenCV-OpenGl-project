import { WebSocketMessage, FrameData, PerformanceStats } from './types';

export class WebSocketClient {
    private ws: WebSocket | null = null;
    private url: string;
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    private reconnectDelay = 1000;
    private isConnected = false;

    private onFrameCallback?: (frame: FrameData) => void;
    private onStatsCallback?: (stats: PerformanceStats) => void;
    private onErrorCallback?: (error: string) => void;
    private onConnectionCallback?: (connected: boolean) => void;

    constructor(url: string = 'ws://localhost:8080/ws') {
        this.url = url;
    }

    public connect(): void {
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
                    const message: WebSocketMessage = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (error) {
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

        } catch (error) {
            console.error('Failed to create WebSocket connection:', error);
            this.onErrorCallback?.('Failed to create WebSocket connection');
        }
    }

    private handleMessage(message: WebSocketMessage): void {
        switch (message.type) {
            case 'frame':
                this.onFrameCallback?.(message.data as FrameData);
                break;
            case 'stats':
                this.onStatsCallback?.(message.data as PerformanceStats);
                break;
            case 'error':
                this.onErrorCallback?.(message.data as string);
                break;
            default:
                console.warn('Unknown message type:', message.type);
        }
    }

    private attemptReconnect(): void {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
            this.onErrorCallback?.('Connection lost. Max reconnection attempts reached.');
        }
    }

    public disconnect(): void {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.isConnected = false;
    }

    public send(message: any): void {
        if (this.ws && this.isConnected) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket not connected');
        }
    }

    // Event handlers
    public onFrame(callback: (frame: FrameData) => void): void {
        this.onFrameCallback = callback;
    }

    public onStats(callback: (stats: PerformanceStats) => void): void {
        this.onStatsCallback = callback;
    }

    public onError(callback: (error: string) => void): void {
        this.onErrorCallback = callback;
    }

    public onConnection(callback: (connected: boolean) => void): void {
        this.onConnectionCallback = callback;
    }

    public isWebSocketConnected(): boolean {
        return this.isConnected && this.ws?.readyState === WebSocket.OPEN;
    }
}
