import { FrameData, PerformanceStats } from './types';
export declare class WebSocketClient {
    private ws;
    private url;
    private reconnectAttempts;
    private maxReconnectAttempts;
    private reconnectDelay;
    private isConnected;
    private onFrameCallback?;
    private onStatsCallback?;
    private onErrorCallback?;
    private onConnectionCallback?;
    constructor(url?: string);
    connect(): void;
    private handleMessage;
    private attemptReconnect;
    disconnect(): void;
    send(message: any): void;
    onFrame(callback: (frame: FrameData) => void): void;
    onStats(callback: (stats: PerformanceStats) => void): void;
    onError(callback: (error: string) => void): void;
    onConnection(callback: (connected: boolean) => void): void;
    isWebSocketConnected(): boolean;
}
//# sourceMappingURL=WebSocketClient.d.ts.map