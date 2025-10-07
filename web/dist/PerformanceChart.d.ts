import { FrameData } from './types';
export declare class PerformanceChart {
    private canvas;
    private ctx;
    private frameHistory;
    private maxDataPoints;
    private chartWidth;
    private chartHeight;
    private padding;
    constructor(canvasId: string);
    private setupCanvas;
    addFrame(frame: FrameData): void;
    render(): void;
    private drawGrid;
    private drawFPSLine;
    private drawProcessingTimeLine;
    private drawLabels;
    clear(): void;
}
//# sourceMappingURL=PerformanceChart.d.ts.map