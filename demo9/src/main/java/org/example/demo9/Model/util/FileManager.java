package org.example.demo9.Model.util;

import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.Song;
import org.example.demo9.Model.Classes.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static void savePlaylistToFile(Playlist playlist, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            Song current = playlist.getHead();
            while (current != null) {
                writer.println(current.getArtistName() + "|" +
                        current.getTrackName() + "|" +
                        current.getReleaseDate() + "|" +
                        current.getGenre() + "|" +
                        current.getLen() + "|" +
                        current.getTopic());
                current = current.getNext();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Playlist loadPlaylistFromFile(String filename, String playlistName) {
        Playlist playlist = new Playlist(playlistName);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    Song song = new Song(
                            parts[0], // artistName
                            parts[1], // trackName
                            Integer.parseInt(parts[2]), // releaseDate
                            parts[3], // genre
                            Double.parseDouble(parts[4]), // len
                            parts[5]  // topic
                    );
                    playlist.addSong(song);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playlist;
    }
}