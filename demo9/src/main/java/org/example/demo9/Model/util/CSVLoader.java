package org.example.demo9.Model.util;

import org.example.demo9.Model.song.Song;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {
    public static List<Song> loadSongs(String filePath) throws IOException {
        List<Song> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {

                String[] parts = splitCSV(line);
                if (parts.length < 6) continue;
                String artist = parts[0];
                String track = parts[1];
                String date = parts[2];
                String genre = parts[3];
                double len = 0;
                try {
                    len = Double.parseDouble(parts[4]);
                } catch (NumberFormatException e) {
                }
                String topic = parts[5];
                out.add(new Song(artist, track, date, genre, len, topic));
            }
        }
        return out;
    }

    private static String[] splitCSV(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (c == ',' && !inQuotes) {
                parts.add(cur.toString().trim()); cur.setLength(0); continue;
            }
            cur.append(c);
        }
        parts.add(cur.toString().trim());
        return parts.toArray(new String[0]);
    }
}
