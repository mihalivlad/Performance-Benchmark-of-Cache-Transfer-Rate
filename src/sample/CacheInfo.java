package sample;

public class CacheInfo {
    private int size;
    private double latency;
    private double missRatio;
    private double missPenalty;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public double getMissRatio() {
        return missRatio;
    }

    public void setMissRatio(double missRatio) {
        this.missRatio = missRatio;
    }

    public double getMissPenalty() {
        return missPenalty;
    }

    public void setMissPenalty(double missPenalty) {
        this.missPenalty = missPenalty;
    }
}
