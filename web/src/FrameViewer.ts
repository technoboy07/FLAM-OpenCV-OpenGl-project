import { FrameData, ProcessingMode, PerformanceStats } from './types';

export class FrameViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private imageData: ImageData | null = null;
    private currentFrame: FrameData | null = null;
    private stats: PerformanceStats;
    private frameHistory: FrameData[] = [];
    private maxHistorySize = 100;

    constructor(canvasId: string) {
        this.canvas = document.getElementById(canvasId) as HTMLCanvasElement;
        if (!this.canvas) {
            throw new Error(`Canvas with id '${canvasId}' not found`);
        }
        
        this.ctx = this.canvas.getContext('2d')!;
        if (!this.ctx) {
            throw new Error('Failed to get 2D context from canvas');
        }

        this.stats = {
            averageFPS: 0,
            maxFPS: 0,
            minFPS: Infinity,
            averageProcessingTime: 0,
            totalFrames: 0,
            uptime: 0
        };

        this.setupCanvas();
    }

    private setupCanvas(): void {
        this.canvas.width = 800;
        this.canvas.height = 600;
        this.canvas.style.border = '2px solid #333';
        this.canvas.style.backgroundColor = '#000';
    }

    public updateFrame(frameData: FrameData): void {
        this.currentFrame = frameData;
        this.addToHistory(frameData);
        this.updateStats();
        this.render();
    }

    private addToHistory(frame: FrameData): void {
        this.frameHistory.push(frame);
        if (this.frameHistory.length > this.maxHistorySize) {
            this.frameHistory.shift();
        }
    }

    private updateStats(): void {
        if (this.frameHistory.length === 0) return;

        const fpsValues = this.frameHistory.map(f => f.fps);
        const processingTimes = this.frameHistory.map(f => f.processingTime);

        this.stats.averageFPS = fpsValues.reduce((a, b) => a + b, 0) / fpsValues.length;
        this.stats.maxFPS = Math.max(...fpsValues);
        this.stats.minFPS = Math.min(...fpsValues);
        this.stats.averageProcessingTime = processingTimes.reduce((a, b) => a + b, 0) / processingTimes.length;
        this.stats.totalFrames = this.frameHistory.length;
        this.stats.uptime = Date.now() - (this.frameHistory[0]?.timestamp || Date.now());
    }

    private render(): void {
        if (!this.currentFrame) return;

        // Clear canvas
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw placeholder content (in real implementation, this would be actual frame data)
        this.drawPlaceholderFrame();
        this.drawOverlay();
    }

    private drawPlaceholderFrame(): void {
        if (!this.currentFrame) return;

        const centerX = this.canvas.width / 2;
        const centerY = this.canvas.height / 2;
        const size = Math.min(this.canvas.width, this.canvas.height) * 0.6;

        // Draw a placeholder frame representation
        this.ctx.strokeStyle = '#4CAF50';
        this.ctx.lineWidth = 3;
        this.ctx.strokeRect(centerX - size/2, centerY - size/2, size, size);

        // Draw processing mode indicator
        this.ctx.fillStyle = '#4CAF50';
        this.ctx.font = 'bold 24px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(this.getModeName(this.currentFrame.processingMode), centerX, centerY);
    }

    private drawOverlay(): void {
        if (!this.currentFrame) return;

        const overlayHeight = 120;
        const overlayY = this.canvas.height - overlayHeight;

        // Draw semi-transparent overlay
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        this.ctx.fillRect(0, overlayY, this.canvas.width, overlayHeight);

        // Draw stats
        this.ctx.fillStyle = '#FFFFFF';
        this.ctx.font = '14px Arial';
        this.ctx.textAlign = 'left';

        const stats = [
            `FPS: ${this.currentFrame.fps.toFixed(1)}`,
            `Resolution: ${this.currentFrame.width}x${this.currentFrame.height}`,
            `Mode: ${this.getModeName(this.currentFrame.processingMode)}`,
            `Processing: ${this.currentFrame.processingTime.toFixed(2)}ms`,
            `Avg FPS: ${this.stats.averageFPS.toFixed(1)}`,
            `Frames: ${this.stats.totalFrames}`
        ];

        stats.forEach((stat, index) => {
            this.ctx.fillText(stat, 10, overlayY + 20 + (index * 18));
        });

        // Draw performance indicator
        this.drawPerformanceIndicator();
    }

    private drawPerformanceIndicator(): void {
        const indicatorX = this.canvas.width - 100;
        const indicatorY = this.canvas.height - 100;
        const indicatorSize = 80;

        // Performance color based on FPS
        let color = '#FF0000'; // Red for low FPS
        if (this.currentFrame!.fps >= 20) color = '#4CAF50'; // Green for good FPS
        else if (this.currentFrame!.fps >= 10) color = '#FF9800'; // Orange for medium FPS

        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.arc(indicatorX, indicatorY, indicatorSize / 2, 0, 2 * Math.PI);
        this.ctx.fill();

        // FPS text in the circle
        this.ctx.fillStyle = '#FFFFFF';
        this.ctx.font = 'bold 12px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(`${this.currentFrame!.fps.toFixed(0)} FPS`, indicatorX, indicatorY + 4);
    }

    private getModeName(mode: ProcessingMode): string {
        switch (mode) {
            case ProcessingMode.GRAYSCALE: return 'Grayscale';
            case ProcessingMode.CANNY_EDGE: return 'Canny Edge';
            case ProcessingMode.BLUR: return 'Blur';
            case ProcessingMode.ORIGINAL: return 'Original';
            default: return 'Unknown';
        }
    }

    public getStats(): PerformanceStats {
        return { ...this.stats };
    }

    public clear(): void {
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.frameHistory = [];
        this.currentFrame = null;
    }
}
