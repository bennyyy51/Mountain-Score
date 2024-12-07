
public class Peak implements Comparable<Peak> {

    // fields
    private double ele;
    private double lat;
    private double lon;
    private String name;
    private double mScore;
    // constructor
    public Peak(double ele, double lat, double lon, String name, double mScore) {
        this.ele = ele;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.mScore = mScore;
    }

    public double ele() {
        return ele;
        
    }

    public double lat() {
        return lat;
        
    }

    public double lon() {
        return lon;
        
    }

    public String name() {
        return name;
        
    }

    public void setmScore(double mScore) {
        this.mScore = mScore;
    }

    public double mScore() {
        return mScore;
        
    }

    public String toString() {
        return ("\"" + name + "\", " + ele + "m (" + lat + ", " + lon + ")");
    }

    public int compareTo(Peak peak2) {
        return Double.compare(ele(), (peak2.ele()));
    }
}