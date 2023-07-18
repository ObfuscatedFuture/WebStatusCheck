import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main implements Runnable {
    private Thread t;
    private String threadName;
    private int ThreadNum;
    public int TotalThreads = 5;
    public int range;
    public int range_r;
    public static int progress = 0;
    public static String currentTask = "";

    public static ArrayList<String> urls = new ArrayList<String>();

    public static File newList = new File("C:\\Users\\Chase\\IdeaProjects\\CamStatus\\src\\newCams.txt");
    public static File oldList = new File("C:\\Users\\Chase\\IdeaProjects\\CamStatus\\src\\camsList.txt");
    public static int oldList_Length = 0;
    Main(String name, int _ThreadNum) {
        threadName = name;
        ThreadNum = _ThreadNum;
        System.out.println("Creating " +  threadName );
    }
    public static void main(String []args) throws IOException
    {

        Scanner list = new Scanner(oldList);


        int i = 0;
        //Deletes dupes as well?
        while(list.hasNextLine())
        {
            urls.add(i, list.nextLine());
            i++;
        }
        System.out.println(urls);
        oldList_Length = urls.size();

        /* Create Threads */
        Main R1 = new Main( "T-1", 1);
        R1.start();

        Main R2 = new Main("T-2", 2);
        R2.start();

        Main R3 = new Main( "T-3", 3);
        R3.start();

        Main R4 = new Main("T-4", 4);
        R4.start();

        Main R5 = new Main("T-5", 5);
        R5.start();
        System.out.println("Progress:        ");
        /* Regex to isolate IP list from urls */
        File newCams = new File(newList.getAbsolutePath());
        File ipList = new File("C:\\Users\\Chase\\IdeaProjects\\CamStatus\\src\\ipsOnly.txt");

        Scanner reading = new Scanner(newCams);

        while(reading.hasNext())
        {
            String nextLine = reading.next();
            /* Gets characters between / and : */
            String regexPattern = "\\/\\/(.*?)\\:";
            Pattern pattern = Pattern.compile(regexPattern);

            Matcher matcher = pattern.matcher(nextLine);
            boolean matchFound = matcher.find();
            /* Isolates IPs from URLS */
            /* Single Thread No Synchronization*/
            if (matchFound) {
                FileWriter myWriter = new FileWriter(ipList, true);
                myWriter.write(matcher.group(1) + System.lineSeparator());
                myWriter.close();
                //System.out.println(matcher.group(1));
            }
        }



    }

    @Override public void run() {


        /*
        First Thread starts at 0 going to size/5
        * */
        if(this.ThreadNum == 1)
        {
            range = urls.size()/TotalThreads;
            range_r=urls.size()%TotalThreads;
            for(int a = 0; a< range+range_r; a++) {
                int code = -1;
                HttpURLConnection connection = null;
                try {
                    URL u = new URL(urls.get(a));
                    try {
                        connection = (HttpURLConnection) u.openConnection();
                        connection.setRequestMethod("HEAD");
                        code = connection.getResponseCode();
                    } catch (Exception e) {

                    }
                    //Currently only filters -1 (No Connection) and 404(Not Found)
                    //Should probably also filter 401 (Unauthorized)
                    urls.get(a);
                    if (code != -1 && (code != 404)) {
                        this.writeFile(urls, a);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                progressBar();
            }
        }
        else
        {
            range = (urls.size()/TotalThreads)*(ThreadNum-1);
            range_r=urls.size()%TotalThreads;
            for(int a = range+range_r; a< (range*ThreadNum)+range_r; a++) {
                int code = -1;
                HttpURLConnection connection = null;
                try {
                    URL u = new URL(urls.get(a));
                    try {
                        connection = (HttpURLConnection) u.openConnection();
                        connection.setRequestMethod("HEAD");
                        code = connection.getResponseCode();
                    } catch (Exception e) {

                    }

                    if (code == 200) {
                        //System.out.println(urls.get(a) + " returned:" + code);
                        this.writeFile(urls, a);

                    } else {
                        //System.out.println(urls.get(a) + " returned:" + code);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                progressBar();
            }
        }
        System.out.println("Thread Exiting . . .");

    }
    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
    public synchronized void writeFile(ArrayList<String> urls, int index) throws IOException {
        FileWriter myWriter = new FileWriter(newList, true);
        myWriter.write(urls.get(index) + System.lineSeparator());
        myWriter.close();
    }
    public synchronized void progressBar()
    {
        progress++;
        for(int x = 0; x < String.valueOf(progress).length()+String.valueOf(oldList_Length).length()+3; x++)
        {
            System.out.print("\b");
        }
        System.out.print(progress+ " / "+oldList_Length);
    }
}
