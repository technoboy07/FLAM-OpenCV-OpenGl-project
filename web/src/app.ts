import { FrameViewer } from './FrameViewer';
import { WebSocketClient } from './WebSocketClient';
import { PerformanceChart } from './PerformanceChart';
import { FrameData, PerformanceStats } from './types';

export class OpenCVWebApp {
    private frameViewer!: FrameViewer;
    private webSocketClient!: WebSocketClient;
    private performanceChart!: PerformanceChart;
    private statusElement!: HTMLElement;
    private connectionStatusElement!: HTMLElement;

    constructor() {
        this.initializeElements();
        this.initializeComponents();
        this.setupEventListeners();
        this.start();
    }

    private initializeElements(): void {
        this.statusElement = document.getElementById('status')!;
        this.connectionStatusElement = document.getElementById('connection-status')!;
    }

    private initializeComponents(): void {
        // Initialize frame viewer
        this.frameViewer = new FrameViewer('frame-canvas');
        
        // Initialize performance chart
        this.performanceChart = new PerformanceChart('performance-chart');
        
        // Initialize WebSocket client with dynamic URL
        const wsUrl = this.getWebSocketUrl();
        this.webSocketClient = new WebSocketClient(wsUrl);
    }
    
    private getWebSocketUrl(): string {
        // Try to get URL from URL parameters first
        const urlParams = new URLSearchParams(window.location.search);
        const wsHost = urlParams.get('ws_host') || 'localhost';
        const wsPort = urlParams.get('ws_port') || '8080';
        
        // Use current host if not specified
        const host = wsHost === 'localhost' ? window.location.hostname : wsHost;
        
        return `ws://${host}:${wsPort}/ws`;
    }

    private setupEventListeners(): void {
        // WebSocket event handlers
        this.webSocketClient.onFrame((frame: FrameData) => {
            this.handleFrameData(frame);
        });

        this.webSocketClient.onStats((stats: PerformanceStats) => {
            this.handleStatsData(stats);
        });

        this.webSocketClient.onError((error: string) => {
            this.handleError(error);
        });

        this.webSocketClient.onConnection((connected: boolean) => {
            this.updateConnectionStatus(connected);
        });

        // Manual refresh button
        const refreshButton = document.getElementById('refresh-button') as HTMLButtonElement;
        refreshButton?.addEventListener('click', () => {
            this.refreshConnection();
        });

        // Clear data button
        const clearButton = document.getElementById('clear-button') as HTMLButtonElement;
        clearButton?.addEventListener('click', () => {
            this.clearData();
        });
    }

    private start(): void {
        this.updateStatus('Initializing...');
        this.webSocketClient.connect();
    }

    private handleFrameData(frame: FrameData): void {
        this.frameViewer.updateFrame(frame);
        this.performanceChart.addFrame(frame);
        this.updateStatus(`Frame received: ${frame.width}x${frame.height} @ ${frame.fps.toFixed(1)} FPS`);
    }

    private handleStatsData(stats: PerformanceStats): void {
        this.updateStatus(`Stats: Avg FPS: ${stats.averageFPS.toFixed(1)}, Total Frames: ${stats.totalFrames}`);
    }

    private handleError(error: string): void {
        this.updateStatus(`Error: ${error}`);
        console.error('Application error:', error);
    }

    private updateConnectionStatus(connected: boolean): void {
        this.connectionStatusElement.textContent = connected ? 'Connected' : 'Disconnected';
        this.connectionStatusElement.className = connected ? 'status-connected' : 'status-disconnected';
    }

    private updateStatus(message: string): void {
        this.statusElement.textContent = message;
        console.log('Status:', message);
    }

    private refreshConnection(): void {
        this.webSocketClient.disconnect();
        setTimeout(() => {
            this.webSocketClient.connect();
        }, 1000);
    }

    private clearData(): void {
        this.frameViewer.clear();
        this.performanceChart.clear();
        this.updateStatus('Data cleared');
    }

    public getStats(): PerformanceStats {
        return this.frameViewer.getStats();
    }
}

// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new OpenCVWebApp();
});
