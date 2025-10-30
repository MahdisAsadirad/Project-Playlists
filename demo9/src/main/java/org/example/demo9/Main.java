package org.example.demo9;

import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.Song;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // مسیر فایل CSV
        String filePath = "C:\\Users\\RGB\\Downloads\\musics.csv";

        // ساخت یک پلی‌لیست جدید
        Playlist myPlaylist = new Playlist("My Favorite Songs");

        // خواندن فایل CSV و اضافه کردن آهنگ‌ها به پلی‌لیست
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // رد کردن هدر
            while ((line = br.readLine()) != null) {
                // جدا کردن مقادیر توسط کاما
                String[] values = line.split(",");
                if (values.length == 6) {
                    Song song = new Song(
                            values[0], // artistName
                            values[1], // trackName
                            values[2], // releaseDate
                            values[3], // genre
                            Double.parseDouble(values[4]), // length
                            values[5]  // topic
                    );
                    myPlaylist.addSong(song);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // چاپ پلی‌لیست کامل
        System.out.println(myPlaylist);

        // مثال: فیلتر کردن بر اساس ژانر
        Playlist popSongs = myPlaylist.filterBy("genre", "pop");
        System.out.println("Filtered by genre 'pop':");
        popSongs.play();

        // مثال: مرتب‌سازی بر اساس نام آهنگ
        myPlaylist.sortBy("track name");
        System.out.println("Sorted by track name:");
        myPlaylist.play();

        // مثال: Shuffle Play
        System.out.println("Shuffle Play:");
        myPlaylist.shufflePlay();
    }
}
