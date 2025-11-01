package org.example.demo9.Controller;

import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.Song;
import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.util.User;

import java.sql.*;
import java.util.*;

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

    public void shufflePlaylists(User user, Scanner scanner) {
        System.out.println("\n🔄 Shuffle Merge Multiple Playlists");
        showUserPlaylists(user.getId());

        System.out.print("Enter playlist IDs to shuffle merge (comma-separated): ");
        String[] playlistIdsStr = scanner.nextLine().split(",");

        if (playlistIdsStr.length < 2) {
            System.out.println("❌ Please enter at least 2 playlist IDs!");
            return;
        }

        System.out.print("Enter name for shuffled playlist: ");
        String newName = scanner.nextLine();

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. بارگذاری پلی‌لیست‌های انتخابی
                List<Playlist> sourcePlaylists = new ArrayList<>();
                Set<Integer> processedPlaylistIds = new HashSet<>();

                for (String playlistIdStr : playlistIdsStr) {
                    int playlistId = Integer.parseInt(playlistIdStr.trim());
                    if (processedPlaylistIds.contains(playlistId)) {
                        System.out.println("⚠️ Skipping duplicate playlist ID: " + playlistId);
                        continue;
                    }

                    Playlist playlist = loadPlaylistFromDatabase(playlistId, user.getId(), conn);
                    if (playlist != null) {
                        sourcePlaylists.add(playlist);
                        processedPlaylistIds.add(playlistId);
                    } else {
                        System.out.println("❌ Playlist not found or access denied: " + playlistId);
                    }
                }

                if (sourcePlaylists.size() < 2) {
                    System.out.println("❌ Need at least 2 valid playlists to merge!");
                    return;
                }

                // 2. ایجاد پلی‌لیست شافل شده
                Playlist shuffledPlaylist = createShuffledPlaylist(sourcePlaylists, newName);

                // 3. ذخیره در دیتابیس
                int newPlaylistId = saveShuffledPlaylistToDatabase(conn, user.getId(), shuffledPlaylist, sourcePlaylists);
                shuffledPlaylist.setId(newPlaylistId);

                conn.commit();
                System.out.println("✅ Shuffled playlist '" + newName + "' created successfully!");
                System.out.println("📊 Merged " + sourcePlaylists.size() + " playlists with " + shuffledPlaylist.getSize() + " unique songs");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error creating shuffled playlist: " + e.getMessage());
        }
    }

    private Playlist createShufflePlaylist(List<Playlist> sourcePlaylists, String newName) {
        Playlist shuffledPlaylist = new Playlist(newName);

        // جمع‌آوری همه آهنگ‌های منحصر به فرد
        Set<Song> uniqueSongs = new HashSet<>();
        for (Playlist playlist : sourcePlaylists) {
            List<Song> songs = playlist.toList();
            uniqueSongs.addAll(songs);
        }

        // تبدیل به لیست و شافل کردن
        List<Song> songList = new ArrayList<>(uniqueSongs);
        Collections.shuffle(songList);

        // اضافه کردن به پلی‌لیست جدید
        for (Song song : songList) {
            shuffledPlaylist.addSong(song);
        }

        return shuffledPlaylist;
    }

    private int saveShufflePlaylistToDatabase(Connection conn, int userId, Playlist shuffledPlaylist,
                                               List<Playlist> sourcePlaylists) throws SQLException {
        // 1. ایجاد پلی‌لیست شافل شده در جدول shuffled_playlists
        String insertPlaylistSql = "INSERT INTO shuffled_playlists (user_id, name) VALUES (?, ?)";
        int shuffledPlaylistId;

        try (PreparedStatement stmt = conn.prepareStatement(insertPlaylistSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, shuffledPlaylist.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                shuffledPlaylistId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to create shuffled playlist!");
            }
        }

        // 2. ذخیره ارتباط با پلی‌لیست‌های منبع
        String insertSourceSql = "INSERT INTO shuffled_playlist_sources (shuffled_playlist_id, original_playlist_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSourceSql)) {
            for (Playlist sourcePlaylist : sourcePlaylists) {
                stmt.setInt(1, shuffledPlaylistId);
                stmt.setInt(2, sourcePlaylist.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        // 3. ذخیره آهنگ‌ها با موقعیت‌های شافل شده
        String insertSongSql = "INSERT INTO shuffled_playlist_songs (shuffled_playlist_id, song_id, user_id, position) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSongSql)) {
            List<Song> songs = shuffledPlaylist.toList();
            for (int i = 0; i < songs.size(); i++) {
                stmt.setInt(1, shuffledPlaylistId);
                stmt.setInt(2, songs.get(i).getId());
                stmt.setInt(3, userId);
                stmt.setInt(4, i);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        return shuffledPlaylistId;
    }

    // متد برای نمایش پلی‌لیست‌های شافل شده
    public void showShufflePlaylists(User user) {
        String sql = """
            SELECT sp.id, sp.name, sp.created_at, 
                   COUNT(sps.song_id) as song_count,
                   GROUP_CONCAT(DISTINCT p.name) as source_playlists
            FROM shuffled_playlists sp
            LEFT JOIN shuffled_playlist_songs sps ON sp.id = sps.shuffled_playlist_id
            LEFT JOIN shuffled_playlist_sources spsrc ON sp.id = spsrc.shuffled_playlist_id
            LEFT JOIN playlists p ON spsrc.original_playlist_id = p.id
            WHERE sp.user_id = ?
            GROUP BY sp.id, sp.name, sp.created_at
            ORDER BY sp.created_at DESC
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🔄 Your Shuffled Playlists:");
            boolean hasPlaylists = false;

            while (rs.next()) {
                System.out.printf(" - [%d] %s (%d songs)%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("song_count"));
                System.out.printf("   Sources: %s%n", rs.getString("source_playlists"));
                System.out.printf("   Created: %s%n", rs.getTimestamp("created_at"));
                System.out.println();
                hasPlaylists = true;
            }

            if (!hasPlaylists) {
                System.out.println("(No shuffled playlists yet!)");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading shuffled playlists: " + e.getMessage());
        }
    }

    // متد برای نمایش آهنگ‌های پلی‌لیست شافل شده
    public void showShuffleSongsFromPlaylists(int shuffledPlaylistId) {
        String sql = """
            SELECT s.id, s.track_name, s.artist_name, s.genre, 
                   sps.position, u.username
            FROM shuffled_playlist_songs sps
            JOIN songs s ON sps.song_id = s.id
            JOIN users u ON sps.user_id = u.id
            WHERE sps.shuffled_playlist_id = ?
            ORDER BY sps.position
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, shuffledPlaylistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎶 Songs in Shuffled Playlist:");
            int position = 1;
            while (rs.next()) {
                System.out.printf("%d. %s - %s (%s) | Position: %d | Added by: %s%n",
                        position++,
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getString("genre"),
                        rs.getInt("position"),
                        rs.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading shuffled playlist songs: " + e.getMessage());
        }
    }
}
