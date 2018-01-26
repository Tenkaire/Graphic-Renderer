package line;

public class LinearInterpolator {
    private double current, delta;

    public LinearInterpolator(double start, double end, double current, double startValue, double endValue){
        this.delta = (endValue - startValue)/(end - start);
        this.current = startValue + ((current - start) * this.delta);
    }

    public LinearInterpolator(double start, double end, double current, double startValue, double endValue, boolean isNegative){
        this.delta = (endValue - startValue)/(end - start);
        this.current = startValue + ((current - start) * this.delta);
        if (isNegative){this.delta = - this.delta;}
    }

    public LinearInterpolator(double start, double change){
        this.current = start;
        this.delta = change;
    }

    public double getValue(){ return current; }
    public void increment(){ current += delta; }

}
