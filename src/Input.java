
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Input {

    public static void main(String[] args) throws Exception {

        double lat = -1.0;
        double lon = -1.0;

        Scanner user = new Scanner(System.in);

        System.out.println("enter latitude : ");
        try {
            lat = user.nextDouble();
        }
        catch (InputMismatchException e) {
            System.out.println("Invalid input, please input a latitude number");
        }

        System.out.println("enter longitude : ");
        try {
            lon = user.nextDouble();
        }
        catch (InputMismatchException e) {
            System.out.println("Invalid input, please input a longitude number");
        }
        

        Score score = new Score(lat, lon);
        List<Peak> list = score.getPeakList();
        score.peakSort(list);
        score.peakClean(list);

        double mScore = score.getTotalmScore(list);
        double topmScore = score.getTopmScore(list);
        user.close();

        System.out.println("top 10 mountains : ");
        for (int i = list.size() - 11; i < list.size(); i++)
        {
            Peak p1 = list.get(i);
            System.out.println(p1 + " mscore : "+ p1.mScore());
        }
        System.out.println("total mountains : " + list.size());
        System.out.println("total mountain score : " + mScore);
        System.out.println("top 10% mountains score : " + topmScore);
  }
    
}
