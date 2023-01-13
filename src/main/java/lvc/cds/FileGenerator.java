package lvc.cds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/*
    Generate files of random words for testing, inserting keywords with a low probability so that they exist.
*/

public class FileGenerator {

    public static void main(String[] args) {
        ArrayList<String> dictionary = new ArrayList<>();
        ArrayList<String> words = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("data\\dictionary.txt"));
            for (String x = in.readLine(); x != null; x = in.readLine()) {
                dictionary.add(x);
            }
            in.close();
            BufferedReader br = new BufferedReader(new FileReader("data\\keywords.txt"));
            for (String x = br.readLine(); x != null; x = br.readLine()) {
                words.add(x);
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e + " uh oh...");
        }
        System.out.println(dictionary.size());
        System.out.println(words.size());

        // Generate files of random words, with a potential to include naughty words
        Random r = new Random();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("data\\test10.txt"));
            for(int i = 0; i < 10_000_000; i++) {
                int p = r.nextInt(1_000_000);
                if (p < 100) { // 0.01% chance we add a naughty word
                    int idx = r.nextInt(words.size());
                    bw.write(words.get(idx) + "\n");
                } else {
                    int idx = r.nextInt(dictionary.size());
                    bw.write(dictionary.get(idx) + "\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            System.out.println(e + " writing error");
        }



    }
    
}
