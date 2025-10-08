import { PerformanceStats } from './types';
export declare class OpenCVWebApp {
    private frameViewer;
    private webSocketClient;
    private performanceChart;
    private statusElement;
    private connectionStatusElement;
    constructor();
    private initializeElements;
    private initializeComponents;
    private getWebSocketUrl;
    private setupEventListeners;
    private start;
    private handleFrameData;
    private handleStatsData;
    private handleError;
    private updateConnectionStatus;
    private updateStatus;
    private refreshConnection;
    private clearData;
    getStats(): PerformanceStats;
}
//# sourceMappingURL=app.d.ts.map