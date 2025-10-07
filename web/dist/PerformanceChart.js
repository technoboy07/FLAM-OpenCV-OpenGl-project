export class PerformanceChart {
    constructor(canvasId) {
        this.frameHistory = [];
        this.maxDataPoints = 50;
        this.padding = 20;
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) {
            throw new Error(`Canvas with id '${canvasId}' not found`);
        }
        this.ctx = this.canvas.getContext('2d');
        if (!this.ctx) {
            throw new Error('Failed to get 2D context from canvas');
        }
        this.chartWidth = this.canvas.width;
        this.chartHeight = this.canvas.height;
        this.setupCanvas();
    }
    setupCanvas() {
        this.canvas.style.border = '1px solid #333';
        this.canvas.style.backgroundColor = '#1a1a1a';
    }
    addFrame(frame) {
        this.frameHistory.push(frame);
        if (this.frameHistory.length > this.maxDataPoints) {
            this.frameHistory.shift();
        }
        this.render();
    }
    render() {
        if (this.frameHistory.length < 2)
            return;
        // Clear canvas
        this.ctx.fillStyle = '#1a1a1a';
        this.ctx.fillRect(0, 0, this.chartWidth, this.chartHeight);
        // Draw grid
        this.drawGrid();
        // Draw FPS line
        this.drawFPSLine();
        // Draw processing time line
        this.drawProcessingTimeLine();
        // Draw labels
        this.drawLabels();
    }
    drawGrid() {
        this.ctx.strokeStyle = '#333';
        this.ctx.lineWidth = 1;
        // Vertical grid lines
        const stepX = (this.chartWidth - 2 * this.padding) / 10;
        for (let i = 0; i <= 10; i++) {
            const x = this.padding + i * stepX;
            this.ctx.beginPath();
            this.ctx.moveTo(x, this.padding);
            this.ctx.lineTo(x, this.chartHeight - this.padding);
            this.ctx.stroke();
        }
        // Horizontal grid lines
        const stepY = (this.chartHeight - 2 * this.padding) / 5;
        for (let i = 0; i <= 5; i++) {
            const y = this.padding + i * stepY;
            this.ctx.beginPath();
            this.ctx.moveTo(this.padding, y);
            this.ctx.lineTo(this.chartWidth - this.padding, y);
            this.ctx.stroke();
        }
    }
    drawFPSLine() {
        if (this.frameHistory.length < 2)
            return;
        const fpsValues = this.frameHistory.map(f => f.fps);
        const maxFPS = Math.max(...fpsValues);
        const minFPS = Math.min(...fpsValues);
        const fpsRange = maxFPS - minFPS || 1;
        this.ctx.strokeStyle = '#4CAF50';
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        this.frameHistory.forEach((frame, index) => {
            const x = this.padding + (index / (this.frameHistory.length - 1)) * (this.chartWidth - 2 * this.padding);
            const normalizedFPS = (frame.fps - minFPS) / fpsRange;
            const y = this.chartHeight - this.padding - normalizedFPS * (this.chartHeight - 2 * this.padding);
            if (index === 0) {
                this.ctx.moveTo(x, y);
            }
            else {
                this.ctx.lineTo(x, y);
            }
        });
        this.ctx.stroke();
    }
    drawProcessingTimeLine() {
        if (this.frameHistory.length < 2)
            return;
        const processingTimes = this.frameHistory.map(f => f.processingTime);
        const maxTime = Math.max(...processingTimes);
        const minTime = Math.min(...processingTimes);
        const timeRange = maxTime - minTime || 1;
        this.ctx.strokeStyle = '#FF9800';
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        this.frameHistory.forEach((frame, index) => {
            const x = this.padding + (index / (this.frameHistory.length - 1)) * (this.chartWidth - 2 * this.padding);
            const normalizedTime = (frame.processingTime - minTime) / timeRange;
            const y = this.chartHeight - this.padding - normalizedTime * (this.chartHeight - 2 * this.padding);
            if (index === 0) {
                this.ctx.moveTo(x, y);
            }
            else {
                this.ctx.lineTo(x, y);
            }
        });
        this.ctx.stroke();
    }
    drawLabels() {
        this.ctx.fillStyle = '#FFFFFF';
        this.ctx.font = '12px Arial';
        this.ctx.textAlign = 'center';
        // FPS label
        this.ctx.fillStyle = '#4CAF50';
        this.ctx.fillText('FPS', this.chartWidth - 30, this.padding + 15);
        // Processing time label
        this.ctx.fillStyle = '#FF9800';
        this.ctx.fillText('Processing Time (ms)', this.chartWidth - 30, this.padding + 35);
        // Current values
        if (this.frameHistory.length > 0) {
            const latest = this.frameHistory[this.frameHistory.length - 1];
            this.ctx.fillStyle = '#FFFFFF';
            this.ctx.font = 'bold 14px Arial';
            this.ctx.textAlign = 'left';
            this.ctx.fillText(`Current FPS: ${latest.fps.toFixed(1)}`, 10, 20);
            this.ctx.fillText(`Processing: ${latest.processingTime.toFixed(2)}ms`, 10, 40);
        }
    }
    clear() {
        this.frameHistory = [];
        this.ctx.fillStyle = '#1a1a1a';
        this.ctx.fillRect(0, 0, this.chartWidth, this.chartHeight);
    }
}
//# sourceMappingURL=PerformanceChart.js.map