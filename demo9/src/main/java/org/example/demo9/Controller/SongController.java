package org.example.demo9.Controller;

import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.util.User;
import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.Song;
import org.example.demo9.Controller.SongController;
import java.util.List;

import java.sql.*;
import java.util.Scanner;

public class SongController {
    private final Database db;

    public SongController(Database db) {
        this.db = db;
    }

    public void showAllSongs() {
        String query = "SELECT id, artist_name, track_name, release_date, genre, len, topic FROM songs";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n -*-*-*-*- All Songs in Library -*-*-*-*-");
            System.out.printf("%-5s | %-25s | %-35s | %-6s | %-10s | %-6s | %-10s%n",
                    "ID", "Artist", "Track", "Year", "Genre", "Len", "Topic");
            System.out.println("-----------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d | %-25s | %-35s | %-6d | %-10s | %-6d | %-10s%n",
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getInt("len"),
                        rs.getString("topic"));
            }
            System.out.println("-----------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error while loading songs: " + e.getMessage());
        }
    }

    public void addSongToPlaylist(int playlistId, int songId, int userId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, userId);

            stmt.executeUpdate();
            System.out.println("Song added successfully by user ID: " + userId);

        } catch (SQLException e) {
            System.out.println("Failed to add song: " + e.getMessage());
        }
    }


    public void removeSongFromPlaylist(int playlistId, int songId) {
        String query = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);

            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("Song removed successfully!");
            else
                System.out.println("Song not found in this playlist.");
        } catch (SQLException e) {
            System.out.println("Failed to remove song: " + e.getMessage());
        }
    }

    public void showSongsInPlaylist(int playlistId) {
        String query = "SELECT s.id, s.track_name, s.artist_name, s.genre, u.username " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "JOIN users u ON ps.user_id = u.id " +
                "WHERE ps.playlist_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎶 Songs in Playlist:");
            while (rs.next()) {
                System.out.printf("%d. %s - %s (%s) | Added by: %s%n",
                        rs.getInt("id"),
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getString("genre"),
                        rs.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading songs: " + e.getMessage());
        }
    }

    public void mergePlaylists(User user, Scanner scanner) {
        System.out.println("\n🎵 Merge Two Playlists");
        showUserPlaylists(user.getId());

        System.out.print("Enter first playlist ID to merge: ");
        int firstId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter second playlist ID to merge: ");
        int secondId = Integer.parseInt(scanner.nextLine());

        if (firstId == secondId) {
            System.out.println("Cannot merge a playlist with itself!");
            return;
        }

        System.out.print("Enter name for merged playlist: ");
        String newName = scanner.nextLine();

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. ایجاد پلی‌لیست جدید در دیتابیس
                int newPlaylistId = createNewPlaylist(conn, user.getId(), newName);

                // 2. بارگذاری پلی‌لیست‌های قدیمی از دیتابیس
                Playlist firstPlaylist = loadPlaylistFromDatabase(firstId, user.getId(), conn);
                Playlist secondPlaylist = loadPlaylistFromDatabase(secondId, user.getId(), conn);

                // 3. ادغام واقعی در سطح لیست پیوندی
                Playlist mergedPlaylist = firstPlaylist.mergeAndCreateNew(secondPlaylist, newName);
                mergedPlaylist.setId(newPlaylistId);

                // 4. ذخیره پلی‌لیست ادغام شده در دیتابیس
                savePlaylistToDatabase(mergedPlaylist, user.getId(), conn);

                // 5. حذف پلی‌لیست‌های قدیمی
                deletePlaylistCompletely(conn, firstId, user.getId());
                deletePlaylistCompletely(conn, secondId, user.getId());

                conn.commit();
                System.out.println("Playlists merged successfully with linked list connection!");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error merging playlists: " + e.getMessage());
        }
    }

    private void showUserPlaylists(int userId) {
        String sql = "SELECT id, name FROM playlists WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Your Playlists:");
            boolean hasPlaylists = false;
            while (rs.next()) {
                System.out.println(" - [" + rs.getInt("id") + "] " + rs.getString("name"));
                hasPlaylists = true;
            }

            if (!hasPlaylists) {
                System.out.println("(No playlists available to merge!)");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error loading playlists: " + e.getMessage());
        }
    }

    private int createNewPlaylist(Connection conn, int userId, String name) throws SQLException {
        String sql = "INSERT INTO playlists (user_id, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Failed to create new playlist!");
            }
        }
    }


    // انتقال آهنگ‌های یک پلی‌لیست به پلی‌لیست جدید
    private void movePlaylistSongs(Connection conn, int sourcePlaylistId, int targetPlaylistId, int userId) throws SQLException {
        String selectSql = "SELECT song_id FROM playlist_songs WHERE playlist_id = ?";
        String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            selectStmt.setInt(1, sourcePlaylistId);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int songId = rs.getInt("song_id");

                // بررسی تکراری نبودن آهنگ در پلی‌لیست مقصد
                if (!isSongInPlaylist(conn, targetPlaylistId, songId)) {
                    insertStmt.setInt(1, targetPlaylistId);
                    insertStmt.setInt(2, songId);
                    insertStmt.setInt(3, userId);
                    insertStmt.executeUpdate();
                }
            }
        }
    }


    // بررسی وجود آهنگ در پلی‌لیست
    private boolean isSongInPlaylist(Connection conn, int playlistId, int songId) throws SQLException {
        String sql = "SELECT 1 FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    //حذف کامل یک پلی لیست
    private void deletePlaylistCompletely(Connection conn, int playlistId, int userId) throws SQLException {
        // ابتدا آهنگ‌های پلی‌لیست را حذف می‌کنیم
        String deleteSongsSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSongsSql)) {
            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
        }

        // سپس خود پلی‌لیست را حذف می‌کنیم
        String deletePlaylistSql = "DELETE FROM playlists WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deletePlaylistSql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }


     // بارگذاری پلی‌لیست از دیتابیس به لیست پیوندی
    private Playlist loadPlaylistFromDatabase(int playlistId, int userId, Connection conn) throws SQLException {
        String sql = "SELECT name FROM playlists WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Playlist playlist = new Playlist(playlistId, rs.getString("name"), userId);
                playlist.loadSongsFromDatabase(conn); // این متد باید در Playlist تعریف شود
                return playlist;
            }
            throw new SQLException("Playlist not found");
        }
    }


     //ذخیره پلی‌لیست ادغام شده در دیتابیس
    private void savePlaylistToDatabase(Playlist playlist, int userId, Connection conn) throws SQLException {
        // آهنگ‌ها از قبل در movePlaylistSongs ذخیره شده‌اند
        // اینجا فقط برای اطمینان دوباره ذخیره می‌کنیم
        String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }

        // اضافه کردن همه آهنگ‌های لیست پیوندی به دیتابیس
        String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            List<Song> songs = playlist.toList();
            for (Song song : songs) {
                stmt.setInt(1, playlist.getId());
                stmt.setInt(2, song.getId());
                stmt.setInt(3, userId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
