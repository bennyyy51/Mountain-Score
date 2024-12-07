
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class Score {

    private double coordinateLat;
    private double coordinateLon;

    //tuning parameters (hours of testing has lead me to use these parameters)
    //cleaning parameters
    //removePercentage is the bottom (percentage) of the total peak list to remove, this helps with cleaning tiny hills out of the data
    private double removePercentage = 25.0;
    //exludePercentage is the top (percentage) of the current peak list that will be allowed to remove peaks from thier close surroundings (lower for more efficient)
    private double excludePercentage = 33.0;
    //seachPercentage is the top (percentage) of the peak list that the higher peak will search though to exclude (lower for more efficient)
    private double seachPercentage = 20.0;
    //distMax is the radius of a circle around a higher peak that will remove lower peaks to avoid irelevant peaks (lower for more efficient)
    private double distMax = 1700.0;

    //visualization parameters
    //radius is the radius in meters from the coordinates that will look for mountains, low=10,000, mid=50,000, high=100,000
    private int radius = 100000;
    //topMountains is the percentage of mountains used for the topmScore which shows the tallest mountains in the system
    private int topMountains = 10;
    //eleW is the weight used in boosting or decreasing elevation significance in the mScore (elevation^eleW)
    private double eleW = 2.0;
    //distW is the weight used in boosting or decreasing distance significance in the mScore (distane^distW)
    private double distW = 0.9;

    public Score(double coordinateLat, double coordinateLon) {
        this.coordinateLat = coordinateLat;
        this.coordinateLon = coordinateLon;

    }

    //returns the average mScore of all of the mountains in a list of Peaks
    //mScore (mountain score) is based on mountain proximity to the original location and mountain elevation,
    //the elevation is altered with a weight of ele^(eleW) and distance with a weight of dist^(distW)
    public double getTotalmScore(List<Peak> list) throws Exception {

        double totalmScore = 0.0;
        double mScore = -1.0;
        
        for (Peak peak : list)
        {
            double ajustedEle = Math.pow(peak.ele(), eleW);
            double distPen = Math.pow((distance(peak.lat(), peak.lon(), coordinateLat, coordinateLon) / 1000), distW);

            mScore = (ajustedEle / distPen) / 100;
            peak.setmScore(mScore);
            totalmScore += mScore;

        }
        totalmScore = totalmScore/list.size();

        return totalmScore;
        
    }

    //returns the average mScore of the (topMountains) percent of mountains in a list of Peaks
    //mScore (mountain score) is based on mountain proximity to the original location and mountain elevation,
    //the elevation is altered with a weight of ele^(eleW) and distance with a weight of dist^(distW)
    public double getTopmScore(List<Peak> list) throws Exception {

        double topmScore = 0.0;
        double mScore = -1.0;
        int n = 100 / topMountains;
        
        for (int i = (list.size() - (list.size() / n)); i < list.size(); i++)
        {
            Peak p1 = list.get(i);

            double ajustedEle = Math.pow(p1.ele(), eleW);
            double distPen = Math.pow((distance(p1.lat(), p1.lon(), coordinateLat, coordinateLon) / 1000), distW);

            mScore = (ajustedEle / distPen) / 100;
            p1.setmScore(mScore);
            topmScore += mScore;

        }
        topmScore = topmScore/(list.size() / 10);

        return topmScore;
    }

    public List<Peak> getPeakList() throws Exception {
        
        List<Peak> list = new ArrayList<Peak>();

        System.out.println("calling api...");

        String query = "[out:json]; node[natural=peak][ele][name](around:" + radius + " , " + coordinateLat + ", " + coordinateLon + "); out;";
        String overpassUrl = "http://overpass-api.de/api/interpreter";
        URL url = new URL(overpassUrl + "?data=" + java.net.URLEncoder.encode(query, "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Scanner fileScnr = new Scanner(new InputStreamReader(connection.getInputStream()));

        String latT="-1", lonT="-1", eleT="-1", name="-1";
        Double lat = -1.0, lon = -1.0, ele = -1.0;

        System.out.println("building list...");

        while (fileScnr.hasNextLine())
        {
            String inputLine = fileScnr.nextLine().trim();

            if (inputLine.startsWith("\"lat\":"))
            {
                latT = inputLine.split(":")[1].trim();
                latT = latT.substring(0, latT.length() - 1);
                lat = Double.parseDouble(latT);
            }

            else if (inputLine.startsWith("\"lon\"")) 
            {
                lonT = inputLine.split(":")[1].trim();
                lonT = lonT.substring(0, lonT.length() - 1);
                lon = Double.parseDouble(lonT);

                while (fileScnr.hasNextLine())
                {
                    inputLine = fileScnr.nextLine().trim();

                    if (inputLine.startsWith("\"ele\""))
                    {
                        eleT = inputLine.split(":")[1].trim();
                        eleT = eleT.substring(1, eleT.length() - 2);

                        try {
                            ele = Double.parseDouble(eleT);
                        }
                        catch (NumberFormatException e) {
                            break;
                        }
                            
                    }   

                    else if (inputLine.startsWith("\"name\""))
                    {
                        name = inputLine.split(":")[1].trim();
                        name = name.substring(1, name.length() - 2);

                        list.add(new Peak(ele, lat, lon, name, -1.0));
                        break;
                    }
                }
            }   
        }
        fileScnr.close();
        return list;
    }

    public List<Peak> peakSort(List<Peak> list) throws Exception {

        System.out.println("sorting list...");

        Collections.sort(list);
        return list;
    }

    public List<Peak> peakClean(List<Peak> list) throws Exception {

       peakRemove(list, removePercentage);
       peakExclude(list, distMax);
        
        return list;
    }

    //needs a sorted list of peaks by elevation, removes the lowest (first) given percentage (percentage) of peaks
    private List<Peak> peakRemove(List<Peak> list, double percentage) throws Exception {

        System.out.println("removing tiny peaks...");

        double n = 100 / percentage;

        Collections.reverse(list);
        double size = list.size();

        for (int i = list.size() - 1; i > (size - (size / n)); i--)
        {
            //removal testing
            //System.out.println("removed " + list.get(i));
            list.remove(i);
        }

        Collections.reverse(list);
        
        return list;
    }

    //excludes peaks in the given proximity (distMax) of a higher peak to avoid duplication
    //only the highest 3rd of peaks in the list get checked for nearby peaks
    //those peaks only check the half of the array from their index
    private List<Peak> peakExclude(List<Peak> list, double distMax) throws Exception {

        double size = list.size();
        List<Peak> exclude = new ArrayList<Peak>();
        double n = 100 / excludePercentage;
        double k = 100 / seachPercentage;

        System.out.println("removing irrelevant peaks...");

        for (int i = list.size() - 1; i > (size - (size / n)); i--)
        {
            Peak p1 = list.get(i);
            
            for (int j = i - 1; j > (size - (size / k)); j--)
            {
                
                Peak p2 = list.get(j);

                if (exclude.contains(p1))
                {
                    break;
                }

                double totalDist = distance(p1.lat(), p1.lon(), p2.lat(), p2.lon());
                //distance testing
                //System.out.println(distance(p1.lat(), p1.lon(), p2.lat(), p2.lon()));

                if (totalDist < distMax)
                {
                    exclude.add(p2);
                    //removal testing
                    //System.out.println(p1.name() + " removed peak " + p2 + " with distance : " + totalDist);
                }
            }
        }
        
        for (Peak peak : exclude) 
        {
            list.remove(peak);
        }

        return list;
    }

    //turns two lat/lon points into a distance between the two points in meters
    private double distance(double lat1, double lon1, double lat2, double lon2) {

        double earthR = 6371000;
        double degLat = (lat2 - lat1) * (Math.PI/180);
        double degLon = (lon2 - lon1) * (Math.PI/180);

        double a = Math.pow(Math.sin(degLat/2), 2) +
                   Math.cos(lat1 * (Math.PI/180)) * 
                   Math.cos(lat2 * (Math.PI/180)) *
                   Math.pow(Math.sin(degLon/2), 2);
        
        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthR * b;

        return dist;
     }
        
}
