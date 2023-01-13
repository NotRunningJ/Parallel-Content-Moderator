package lvc.cds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
    my second attempt at a concurrent solution where I only created 8 threads
    (one per core on good CPU's, two per core on most) to read in all the files. This takes out some overhead
    which would have been created if thousands of files needed to be searched. With small 
    sets of files a speedup is not noticed, but surley there would be one for large
    sets of files. Still about 2/3 the time of the serial version.
*/

public class ThreadedSearcher {

    /*
        All global variables - used by many threads.
    */

    private static String DIR; //subdirectory within the project - where the data is held

    private static ArrayList<String> words; // list of keywords
    private static ArrayList<Integer> counts; // list of total keywords occurences across all files

    private static ArrayList<String> moderated; // list of files to be moderated


    /*
        Worker subclass to divy up the total work over 8 threads, each Worker
        has a list of files to search
    */
    private static class Worker extends Thread{

        ArrayList<String> workingList; // list of files for this guy to work on

        public Worker(ArrayList<String> files) {
            this.workingList = files;
        }

        public void run() {
            for(String s : this.workingList) {
                int sum = search(s);
                System.out.println(s + " has " + sum + " keywords");
                if(sum >= 1_000) { // seemed to be a nice cutoff
                    // changing shared memory
                    synchronized(moderated) {
                        moderated.add(s);
                    }
                }
            }
        }
    }


    /*
        Generate the words and counts list from the input list, make 
        them lowercase for ease of searching
    */
    private static void genLists(String file) {
        words = new ArrayList<>();
        counts = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                x = x.toLowerCase();
                words.add(x);
                counts.add(0);
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e + " in genLists");
        }
    }


    /*
        searches through a file to find occurences of certain keywords, incrementing the total number of times
        each keyword has been seen across all files in the program, as well as keeping a tally of the total
        number of keywords seen within each file, returning the sum for each individual file.
    */
    private static int search(String file) {

        // search through a file looking for a naugty word or phrase, incrementing counts when necessary
        int sum = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(DIR + file));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                x = x.toLowerCase();
                //search for word, increment counts where necessary 
                for(int i = 0; i < words.size(); i++) {
                    if(x.equals(words.get(i))) {
                        sum++;
                        // changing shared memory
                        synchronized(counts) {
                            counts.set(i, counts.get(i) + 1);
                        }
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e + " in search");
        }
        return sum;
    }



    public static void main(String[] args) {
        moderated = new ArrayList<>(); // initialize at the start, used by all Workers

        //get all the files ready
        Scanner sc = new Scanner(System.in);
        System.out.println("Please give me the name of your subdirectory folder");
        String separator = System.getProperty("file.separator"); // works across different OS
        DIR = sc.nextLine() + separator;
        System.out.println("Input keywords file name: ");
        String keyWordFile = DIR + sc.nextLine();
        System.out.println("Input file name consisting of files you wish to scan: ");
        String fileNames = DIR + sc.nextLine();
        sc.close();
        genLists(keyWordFile);


        // Reading in all the files to be searched
        ArrayList<String> files = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileNames));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                files.add(x);
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e + " in main");
        }

        long start = System.nanoTime();

        ArrayList<ArrayList<String>> work = new ArrayList<>();
        //create lists to hold the work for 8 workers
        for(int i = 0; i < 8; i++) {
            ArrayList<String> thWork = new ArrayList<>();
            work.add(thWork);
        }


        // divide up all the files across 8 lists
        for(int i = 0; i < files.size(); i++) {
            int t = i % 7; // 8 threads, divy up the work this way
            work.get(t).add(files.get(i)); // list of lists so this line is nice
        }


        // Give the workers their work....and make them start working
        ArrayList<Worker> workers = new ArrayList<>();
        for(int i = 0; i < work.size(); i++) {
            Worker w = new Worker(work.get(i));
            w.start();
            workers.add(w);
        }

        // wait for the threads to finish working
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println(e + " Threads not happy");
            }
        }

        long finish = System.nanoTime(); // interesting part has finished

        // print which files should be moderated (hit a number of keywords)
        for(String s : moderated) {
            System.out.println(s + " must be moderated");
        }

        // print how many times each keyword appeared across all files
        for(int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i) + " appeared a total of " + counts.get(i) + " times");
        }

        System.out.println("finished in " + ((finish-start) / 1_000_000_000.00) + " seconds");

    }
    
}
