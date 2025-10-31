package org.example.demo9.Model.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;

public class SongImporter {

    public static void importCSV(String csvFilePath, Connection conn) {
        String query = "INSERT INTO songs (artist_name, track_name, release_date, genre, len, topic) VALUES (?, ?, ?, ?, ?, ?)";
        int importedCount = 0;
        int skippedCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
             PreparedStatement ps = conn.prepareStatement(query)) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                String[] data = line.split(",");

                if (data.length < 6) {
                    System.out.println("⚠️ Skipped line: " + Arrays.toString(data));
                    skippedCount++;
                    continue;
                }

                try {
                    String artist = data[0].trim();
                    String track = data[1].trim();
                    int year = Integer.parseInt(data[2].trim());
                    String genre = data[3].trim();

                    double len = 0;
                    try {
                        len = Double.parseDouble(data[4].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Invalid length: " + data[4]);
                    }

                    String topic = data[5].trim();

                    ps.setString(1, artist);
                    ps.setString(2, track);
                    ps.setInt(3, year);
                    ps.setString(4, genre);
                    ps.setDouble(5, len);
                    ps.setString(6, topic);
                    ps.addBatch();
                    importedCount++;

                } catch (Exception e) {
                    System.out.println("⚠️ Skipped malformed row: " + Arrays.toString(data));
                    skippedCount++;
                }
            }

            ps.executeBatch();
            System.out.println("✅ Imported: " + importedCount + " songs");
            System.out.println("🚫 Skipped: " + skippedCount + " lines");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
