import { FrameData, PerformanceStats } from './types';
export declare class FrameViewer {
    private canvas;
    private ctx;
    private imageData;
    private currentFrame;
    private stats;
    private frameHistory;
    private maxHistorySize;
    constructor(canvasId: string);
    private setupCanvas;
    updateFrame(frameData: FrameData): void;
    private addToHistory;
    private updateStats;
    private render;
    private drawPlaceholderFrame;
    private drawOverlay;
    private drawPerformanceIndicator;
    private getModeName;
    getStats(): PerformanceStats;
    clear(): void;
}
//# sourceMappingURL=FrameViewer.d.ts.map