package org.example.demo9.Model.util;

import org.example.demo9.Model.song.Song;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<Song> readSongsFromCSV(String path) {
        List<Song> songs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // رد کردن هدر CSV
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // -1 برای حفظ خالی‌ها
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
