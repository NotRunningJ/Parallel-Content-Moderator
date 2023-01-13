package lvc.cds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/*
    a serial program to search through a list of files and scan them for keywords.
*/

public class Moderator {

    private static String DIR; //subdirectory within the project - where the data is held

    private static ArrayList<String> words; // list of keywords 
    private static ArrayList<Integer> counts; // list of number of occurences of each keyword accross all files

    private static ArrayList<String> moderated; // list of files to be moderated

    // Hashmap for fast access, and adding?
    private static HashMap<String, Integer> words2;

    /*
        Generate the words and counts list from the input list, make 
        them lowercase for ease of searching
    */
    private static void genLists(String file) {
        words2 = new HashMap<>();
        words = new ArrayList<>();
        counts = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                x = x.toLowerCase();
                words.add(x);
                counts.add(0);
                words2.put(x, 0); // add value 0 as placeholder
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
        int sum = 0;
        // search through a file looking for a naugty word or phrase, incrementing counts when necessary
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                x = x.toLowerCase();
                //search for word, increment counts where necessary 
                for(int i = 0; i < words.size(); i++) {
                    if(x.equals(words.get(i))) {
                        sum++;
                        counts.set(i, counts.get(i) + 1);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e + " in search");
        }
        return sum;
    }

    
    public static void main(String[] args) throws IOException {

        moderated = new ArrayList<>();

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
        for (String f : files) {
            int tot = search(DIR + f);
            if(tot >= 1_000) { // seems to be a good cutoff.
                moderated.add(f);
            }
            System.out.println(f + " Has " + tot + " keywords");
        }

        long finish = System.nanoTime();

        // prints the files that need to be moderated
        for(String s : moderated) {
            System.out.println(s + " needs moderated");
        }

        // prints the total number of occurences of each keyword across all files
        for(int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i) + " appeared a total of " + counts.get(i) + " times");
        }

        System.out.println("finished in " + ((finish-start) / 1_000_000_000.00) + " seconds");

    }
}
