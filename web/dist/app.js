import { FrameViewer } from './FrameViewer';
import { WebSocketClient } from './WebSocketClient';
import { PerformanceChart } from './PerformanceChart';
export class OpenCVWebApp {
    constructor() {
        this.initializeElements();
        this.initializeComponents();
        this.setupEventListeners();
        this.start();
    }
    initializeElements() {
        this.statusElement = document.getElementById('status');
        this.connectionStatusElement = document.getElementById('connection-status');
    }
    initializeComponents() {
        // Initialize frame viewer
        this.frameViewer = new FrameViewer('frame-canvas');
        // Initialize performance chart
        this.performanceChart = new PerformanceChart('performance-chart');
        // Initialize WebSocket client with dynamic URL
        const wsUrl = this.getWebSocketUrl();
        this.webSocketClient = new WebSocketClient(wsUrl);
    }
    getWebSocketUrl() {
        // Try to get URL from URL parameters first
        const urlParams = new URLSearchParams(window.location.search);
        const wsHost = urlParams.get('ws_host') || 'localhost';
        const wsPort = urlParams.get('ws_port') || '8080';
        // Use current host if not specified
        const host = wsHost === 'localhost' ? window.location.hostname : wsHost;
        return `ws://${host}:${wsPort}/ws`;
    }
    setupEventListeners() {
        // WebSocket event handlers
        this.webSocketClient.onFrame((frame) => {
            this.handleFrameData(frame);
        });
        this.webSocketClient.onStats((stats) => {
            this.handleStatsData(stats);
        });
        this.webSocketClient.onError((error) => {
            this.handleError(error);
        });
        this.webSocketClient.onConnection((connected) => {
            this.updateConnectionStatus(connected);
        });
        // Manual refresh button
        const refreshButton = document.getElementById('refresh-button');
        refreshButton?.addEventListener('click', () => {
            this.refreshConnection();
        });
        // Clear data button
        const clearButton = document.getElementById('clear-button');
        clearButton?.addEventListener('click', () => {
            this.clearData();
        });
    }
    start() {
        this.updateStatus('Initializing...');
        this.webSocketClient.connect();
    }
    handleFrameData(frame) {
        this.frameViewer.updateFrame(frame);
        this.performanceChart.addFrame(frame);
        this.updateStatus(`Frame received: ${frame.width}x${frame.height} @ ${frame.fps.toFixed(1)} FPS`);
    }
    handleStatsData(stats) {
        this.updateStatus(`Stats: Avg FPS: ${stats.averageFPS.toFixed(1)}, Total Frames: ${stats.totalFrames}`);
    }
    handleError(error) {
        this.updateStatus(`Error: ${error}`);
        console.error('Application error:', error);
    }
    updateConnectionStatus(connected) {
        this.connectionStatusElement.textContent = connected ? 'Connected' : 'Disconnected';
        this.connectionStatusElement.className = connected ? 'status-connected' : 'status-disconnected';
    }
    updateStatus(message) {
        this.statusElement.textContent = message;
        console.log('Status:', message);
    }
    refreshConnection() {
        this.webSocketClient.disconnect();
        setTimeout(() => {
            this.webSocketClient.connect();
        }, 1000);
    }
    clearData() {
        this.frameViewer.clear();
        this.performanceChart.clear();
        this.updateStatus('Data cleared');
    }
    getStats() {
        return this.frameViewer.getStats();
    }
}
// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new OpenCVWebApp();
});
//# sourceMappingURL=app.js.map