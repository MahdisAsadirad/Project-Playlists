package org.example.demo9.Model.song;

import java.io.*;
import java.util.*;

public class CSVReader {
    public static List<Song> readSongsFromCSV(String path) {
        List<Song> songs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length >= 6) {
                    String artist = values[0].trim();
                    String track = values[1].trim();
                    String release = values[2].trim();
                    String genre = values[3].trim();
                    double length = Double.parseDouble(values[4].trim());
                    String topic = values[5].trim();

                    songs.add(new Song(artist, track, release, genre, length, topic));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return songs;
    }
}
