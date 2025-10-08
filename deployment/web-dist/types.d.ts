export interface FrameData {
    timestamp: number;
    width: number;
    height: number;
    fps: number;
    processingMode: ProcessingMode;
    processingTime: number;
}
export declare enum ProcessingMode {
    GRAYSCALE = 0,
    CANNY_EDGE = 1,
    BLUR = 2,
    ORIGINAL = 3
}
export interface PerformanceStats {
    averageFPS: number;
    maxFPS: number;
    minFPS: number;
    averageProcessingTime: number;
    totalFrames: number;
    uptime: number;
}
export interface WebSocketMessage {
    type: 'frame' | 'stats' | 'error';
    data: FrameData | PerformanceStats | string;
}
//# sourceMappingURL=types.d.ts.map