package lvc.cds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
    my first attempt at a concurrent solution to the problem where I created one 
    thread per file, which would not scale well if enough files were tossed at the program. There would've
    been context-switching across multiple cores and would create a lot over overhead. Runtimes are about 2/3
    of the serial solution with 10 testing files of size 10 million lines.
 */

public class ThreadedModerator {

    /*
        All global variables - used by many threads.
    */

    private static String DIR; //subdirectory within the project - where the data is held

    private static ArrayList<String> words; // list of keywords
    private static ArrayList<Integer> counts; // list of total occurences of each keyword across all files

    private static ArrayList<String> moderated; // list of files to be moderated


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
        number of keywords seen within each file. 
        Instead of returning the sum, make the print statement and determine moderation in the function
    */
    private static void search(String file) {
        moderated = new ArrayList<>();
        int sum = 0;
        // search through a file looking for a naugty word or phrase, incrementing counts when necessary
        try {
            BufferedReader in = new BufferedReader(new FileReader(DIR + file));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                x = x.toLowerCase();
                //search for word, increment counts where necessary 
                for(int i = 0; i < words.size(); i++) {
                    if(x.equals(words.get(i))) {
                        sum++;
                        synchronized(counts) {
                            counts.set(i, counts.get(i) + 1);
                        }
                    }
                }
            }
            System.out.println(file + " has " + sum + " keywords");
            in.close();
        } catch (IOException e) {
            System.out.println(e + " in search");
        }
        if(sum >= 1_000) {
            synchronized(moderated) {
                moderated.add(file);
            }
        }
    }



    public static void main(String[] args) {
        ArrayList<Thread> workers = new ArrayList<>();

        //get all the files ready
        Scanner sc = new Scanner(System.in);
        System.out.println("Please give me the name of your subdirectory folder");
        String separator = System.getProperty("file.separator"); // works across different OS
        DIR = sc.nextLine() + separator;
        System.out.println("Input keywords file name: ");
        String naugtyFile = DIR + sc.nextLine();
        System.out.println("Input file name consisting of files you wish to scan: ");
        String fileNames = DIR + sc.nextLine();
        sc.close();
        genLists(naugtyFile);
        ArrayList<String> files = new ArrayList<>();

        // read in all the filenames
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

        // search through the files, one thread per file
        for(String f : files) {
            final String temp = f;
            Thread t = new Thread(() -> search(temp)); // create a thread with a lambda telling it what to do
            t.start();
            workers.add(t); 
        }

        // wait for the threads to finish working
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println(e + " Threads not happy");
            }
        }

        long finish = System.nanoTime();

        // print the files that need moderated
        for(String s : moderated) {
            System.out.println(s + " must be moderated");
        }

        // print the total number of occurences of each keyword across all files
        for(int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i) + " appeared a total of " + counts.get(i) + " times");
        }

        System.out.println("finished in " + ((finish-start) / 1_000_000_000.00) + " seconds");

    }
    
}
