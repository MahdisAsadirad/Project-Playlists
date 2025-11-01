package org.example.demo9.Controller;

import org.example.demo9.Model.Classes.SongNode;
import org.example.demo9.Model.util.Database;
import org.example.demo9.Model.Classes.User;
import org.example.demo9.Model.Classes.Playlist;
import org.example.demo9.Model.Classes.Song;

import java.util.*;

import java.sql.*;

public class SongController {
    private final Database db;

    public SongController(Database db) {
        this.db = db;
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
                int newPlaylistId = createNewPlaylist(conn, user.getId(), newName);

                Playlist firstPlaylist = loadPlaylistFromDatabase(firstId, user.getId(), conn);
                Playlist secondPlaylist = loadPlaylistFromDatabase(secondId, user.getId(), conn);

                // ادغام
                Playlist mergedPlaylist = secondPlaylist.mergeAndCreateNew(firstPlaylist, newName);
                mergedPlaylist.setId(newPlaylistId);

                savePlaylistToDatabase(mergedPlaylist, user.getId(), conn);

                // حذف پلی‌لیست‌های قدیمی
                deletePlaylistCompletely(conn, firstId, user.getId());
                deletePlaylistCompletely(conn, secondId, user.getId());

                conn.commit();
                System.out.println("Playlists merged successfully with linked list connection!");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error merging playlists: " + e.getMessage());
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

    public void shufflePlaylists(User user, Scanner scanner) {
        System.out.println("\nShuffle Merge Multiple Playlists");
        showUserPlaylists(user.getId());

        System.out.print("Enter playlist IDs to shuffle merge (comma-separated): ");
        String[] playlistIdsStr = scanner.nextLine().split(",");

        if (playlistIdsStr.length < 2) {
            System.out.println("*** Please enter at least 2 playlist IDs!!! ***");
            return;
        }

        System.out.print("Enter name for shuffled playlist: ");
        String newName = scanner.nextLine();

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
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
                        System.out.println("playlist not found or access denied: " + playlistId);
                    }
                }

                if (sourcePlaylists.size() < 2) {
                    System.out.println("❌ Need at least 2 valid playlists to merge!");
                    return;
                }

                // ایجاد پلی‌لیست شافل شده
                Playlist shuffledPlaylist = createShufflePlaylist(sourcePlaylists, newName);

                // ذخیره در دیتابیس
                int newPlaylistId = saveShuffleToDatabase(conn, user.getId(), shuffledPlaylist, sourcePlaylists);
                shuffledPlaylist.setId(newPlaylistId);

                conn.commit();
                System.out.println(" Shuffled playlist '" + newName + "' created successfully!");
                System.out.println("Merged " + sourcePlaylists.size() + " playlists with " + shuffledPlaylist.getSize() + " unique songs ^-^");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error creating shuffled playlist: " + e.getMessage());
        }
    }

    private Playlist createShufflePlaylist(List<Playlist> sourcePlaylists, String newName) {
        Playlist shuffledPlaylist = new Playlist(newName);

        Playlist tempList = new Playlist("temp");
        for (Playlist playlist : sourcePlaylists) {
            SongNode current = playlist.getHead();
            while (current != null) {
                if (!tempList.containsSong(current.getData())) {
                    tempList.addSong(current.getData());
                }
                current = current.getNext();
            }
        }

        List<Song> songsArray = tempList.toList();
        Collections.shuffle(songsArray);

        for (Song song : songsArray) {
            shuffledPlaylist.addSong(song);
        }

        return shuffledPlaylist;
    }

    private int saveShuffleToDatabase(Connection conn, int userId, Playlist shuffledPlaylist,
                                      List<Playlist> sourcePlaylists) throws SQLException {

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

        String insertSourceSql = "INSERT INTO shuffled_playlist_sources (shuffled_playlist_id, original_playlist_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSourceSql)) {
            for (Playlist sourcePlaylist : sourcePlaylists) {
                stmt.setInt(1, shuffledPlaylistId);
                stmt.setInt(2, sourcePlaylist.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        // ذخیره آهنگ‌ها با موقعیت‌های شافل شد
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

    public void showShufflePlaylists(User user) {
        // جایگزینی Text Block با String معمولی
        String sql = "SELECT sp.id, sp.name, sp.created_at, " +
                "COUNT(sps.song_id) as song_count, " +
                "GROUP_CONCAT(DISTINCT p.name) as source_playlists " +
                "FROM shuffled_playlists sp " +
                "LEFT JOIN shuffled_playlist_songs sps ON sp.id = sps.shuffled_playlist_id " +
                "LEFT JOIN shuffled_playlist_sources spsrc ON sp.id = spsrc.shuffled_playlist_id " +
                "LEFT JOIN playlists p ON spsrc.original_playlist_id = p.id " +
                "WHERE sp.user_id = ? " +
                "GROUP BY sp.id, sp.name, sp.created_at " +
                "ORDER BY sp.created_at DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nYour Shuffled Playlists:");
            boolean hasPlaylists = false;

            while (rs.next()) {
                System.out.printf(" - [%d] %s (%d songs)%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("song_count"));
                System.out.printf("   Sources: %s%n", rs.getString("source_playlists"));
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

    public void showShuffleSongs(int shuffledPlaylistId) {
        // جایگزینی Text Block با String معمولی
        String sql = "SELECT s.id, s.track_name, s.artist_name, s.genre, " +
                "sps.position, u.username " +
                "FROM shuffled_playlist_songs sps " +
                "JOIN songs s ON sps.song_id = s.id " +
                "JOIN users u ON sps.user_id = u.id " +
                "WHERE sps.shuffled_playlist_id = ? " +
                "ORDER BY sps.position";

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
            System.out.println("Error loading shuffled playlist songs: " + e.getMessage());
        }
    }

    public void sortPlaylist(User user, Scanner scanner) {
        System.out.println("\n🎵 Sort Playlist");
        showUserPlaylists(user.getId());

        System.out.print("Enter playlist ID to sort: ");
        int playlistId = Integer.parseInt(scanner.nextLine());

        System.out.println("\nSort by:");
        System.out.println("1. Track Name");
        System.out.println("2. Artist Name");
        System.out.println("3. Release Date");
        System.out.print("Choose criteria: ");
        String choice = scanner.nextLine();

        String criteria;
        switch (choice) {
            case "1":
                criteria = "track name";
                break;
            case "2":
                criteria = "artist name";
                break;
            case "3":
                criteria = "release date";
                break;
            default:
                System.out.println("Invalid choice!");
                return;
        }

        try (Connection conn = db.getConnection()) {
            // بارگذاری پلی‌لیست از دیتابیس
            Playlist playlist = loadPlaylistFromDatabase(playlistId, user.getId(), conn);

            if (playlist != null) {
                // سورت کردن لینک لیست
                playlist.sortLinkedlistBy(criteria);

                // ذخیره تغییرات در دیتابیس
                savePlaylistToDatabase(playlist, user.getId(), conn);

                System.out.println("Playlist sorted successfully by " + criteria + "!");
                System.out.println("Sorted Playlist:");
                System.out.println(playlist);
            } else {
                System.out.println("Playlist not found or access denied!");
            }

        } catch (SQLException e) {
            System.out.println("Error sorting playlist: " + e.getMessage());
        }
    }

    private Playlist loadPlaylistFromDatabase(int playlistId, int userId, Connection conn) throws SQLException {
        String sql = "SELECT name FROM playlists WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Playlist playlist = new Playlist(playlistId, rs.getString("name"), userId);
                playlist.loadSongsFromDatabase(conn);
                return playlist;
            }
            return null;
        }
    }

    private void savePlaylistToDatabase(Playlist playlist, int userId, Connection conn) throws SQLException {
        // حذف آهنگ‌های قدیمی
        String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, playlist.getId());
            stmt.executeUpdate();
        }

        // اضافه کردن آهنگ‌های سورت شده
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
                System.out.println("(No playlists available!)");
            }
        } catch (SQLException e) {
            System.out.println("Error loading playlists: " + e.getMessage());
        }
    }

    public void filterPlaylist(User user, Scanner scanner) {
        System.out.println("\n🎵 Filter Playlist");
        showUserPlaylists(user.getId());

        System.out.print("Enter playlist ID to filter: ");
        int playlistId = Integer.parseInt(scanner.nextLine());

        System.out.println("\nFilter by:");
        System.out.println("1. Genre");
        System.out.println("2. Artist Name");
        System.out.println("3. Release Year");
        System.out.println("4. Topic");
        System.out.print("Choose criteria: ");
        String choice = scanner.nextLine();

        String criteria;
        String displayCriteria;

        switch (choice) {
            case "1":
                criteria = "genre";
                displayCriteria = "Genre";
                break;
            case "2":
                criteria = "artist_name";
                displayCriteria = "Artist Name";
                break;
            case "3":
                criteria = "release_date";
                displayCriteria = "Release Year";
                break;
            case "4":
                criteria = "topic";
                displayCriteria = "Topic";
                break;
            default:
                System.out.println("Invalid choice!");
                return;
        }

        System.out.print("Enter " + displayCriteria + " to filter by: ");
        String filterValue = scanner.nextLine();

        System.out.print("Enter name for filtered playlist: ");
        String newPlaylistName = scanner.nextLine();

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Playlist originalPlaylist = loadPlaylistFromDatabase(playlistId, user.getId(), conn);

                if (originalPlaylist == null) {
                    System.out.println("Playlist not found or access denied!");
                    return;
                }

                Playlist filteredPlaylist = createFilteredPlaylist(originalPlaylist, criteria, filterValue, newPlaylistName);

                if (filteredPlaylist.getSize() == 0) {
                    System.out.println("❌ No songs found matching the filter criteria!");
                    return;
                }

                int newPlaylistId = saveFilteredPlaylistToDatabase(conn, user.getId(), filteredPlaylist,
                        playlistId, criteria, filterValue);
                filteredPlaylist.setId(newPlaylistId);

                conn.commit();

                System.out.println("\n✅ Filtered playlist created successfully!");
                System.out.println("📊 Filtered by: " + displayCriteria + " = '" + filterValue + "'");
                System.out.println("🎵 Found " + filteredPlaylist.getSize() + " songs");
                System.out.println("🔗 Original playlist: " + originalPlaylist.getName());
                System.out.println("💾 Saved as: " + newPlaylistName);

                System.out.println("\n🎶 Filtered Songs:");
                System.out.println(filteredPlaylist);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error filtering playlist: " + e.getMessage());
        }
    }

    private Playlist createFilteredPlaylist(Playlist originalPlaylist, String criteria,
                                            String filterValue, String newName) {
        Playlist filteredPlaylist = new Playlist(newName);

        SongNode current = originalPlaylist.head;
        while (current != null) {
            boolean matches = false;

            switch (criteria.toLowerCase()) {
                case "genre":
                    matches = current.getData().getGenre().equalsIgnoreCase(filterValue);
                    break;
                case "artist_name":
                    matches = current.getData().getArtistName().equalsIgnoreCase(filterValue);
                    break;
                case "release_date":
                    try {
                        int filterYear = Integer.parseInt(filterValue);
                        matches = (current.getData().getReleaseDate() == filterYear);
                    } catch (NumberFormatException e) {
                        matches = false;
                    }
                    break;
                case "topic":
                    matches = current.getData().getTopic().equalsIgnoreCase(filterValue);
                    break;
            }

            if (matches) {
                filteredPlaylist.addSong(current.getData());
            }
            current = current.getNext();
        }

        return filteredPlaylist;
    }

    private int saveFilteredPlaylistToDatabase(Connection conn, int userId, Playlist filteredPlaylist,
                                               int originalPlaylistId, String criteria, String filterValue) throws SQLException {

        String insertPlaylistSql = "INSERT INTO filtered_playlists (user_id, name, original_playlist_id, filter_criteria, filter_value) VALUES (?, ?, ?, ?, ?)";
        int filteredPlaylistId;

        try (PreparedStatement stmt = conn.prepareStatement(insertPlaylistSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, filteredPlaylist.getName());
            stmt.setInt(3, originalPlaylistId);
            stmt.setString(4, criteria);
            stmt.setString(5, filterValue);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                filteredPlaylistId = rs.getInt(1);
            } else {
                throw new SQLException("Failed to create filtered playlist!");
            }
        }

        String insertSongSql = "INSERT INTO filtered_playlist_songs (filtered_playlist_id, song_id, user_id, position) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSongSql)) {
            List<Song> songs = filteredPlaylist.toList();
            for (int i = 0; i < songs.size(); i++) {
                stmt.setInt(1, filteredPlaylistId);
                stmt.setInt(2, songs.get(i).getId());
                stmt.setInt(3, userId);
                stmt.setInt(4, i);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }

        return filteredPlaylistId;
    }

    public void showFilteredPlaylists(User user) {
        // جایگزینی Text Block با String معمولی
        String sql = "SELECT fp.id, fp.name, fp.filter_criteria, fp.filter_value, " +
                "fp.created_at, COUNT(fps.song_id) as song_count, " +
                "p.name as original_playlist, u.username " +
                "FROM filtered_playlists fp " +
                "LEFT JOIN filtered_playlist_songs fps ON fp.id = fps.filtered_playlist_id " +
                "LEFT JOIN playlists p ON fp.original_playlist_id = p.id " +
                "LEFT JOIN users u ON fp.user_id = u.id " +
                "WHERE fp.user_id = ? " +
                "GROUP BY fp.id, fp.name, fp.filter_criteria, fp.filter_value, fp.created_at, p.name, u.username " +
                "ORDER BY fp.created_at DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎛️ Your Filtered Playlists:");
            boolean hasPlaylists = false;

            while (rs.next()) {
                System.out.printf(" - [%d] %s (%d songs)%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("song_count"));
                System.out.printf("   🎯 Filter: %s = '%s'%n",
                        getCriteriaDisplayName(rs.getString("filter_criteria")),
                        rs.getString("filter_value"));
                System.out.printf("   📁 Source: %s%n", rs.getString("original_playlist"));
                System.out.printf("   👤 Created by: %s%n", rs.getString("username"));
                System.out.println();
                hasPlaylists = true;
            }

            if (!hasPlaylists) {
                System.out.println("(No filtered playlists yet!)");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading filtered playlists: " + e.getMessage());
        }
    }

    public void showFilteredSongs(int filteredPlaylistId) {
        // جایگزینی Text Block با String معمولی
        String sql = "SELECT s.id, s.track_name, s.artist_name, s.genre, s.release_date, " +
                "fps.position, u.username " +
                "FROM filtered_playlist_songs fps " +
                "JOIN songs s ON fps.song_id = s.id " +
                "JOIN users u ON fps.user_id = u.id " +
                "WHERE fps.filtered_playlist_id = ? " +
                "ORDER BY fps.position";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, filteredPlaylistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n🎶 Songs in Filtered Playlist:");
            int position = 1;
            while (rs.next()) {
                System.out.printf("%d. %s - %s (%s, %s) | Added by: %s%n",
                        position++,
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getString("genre"),
                        rs.getString("release_date"),
                        rs.getString("username"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading filtered playlist songs: " + e.getMessage());
        }
    }

    private String getCriteriaDisplayName(String criteria) {
        switch (criteria) {
            case "genre":
                return "Genre";
            case "artist_name":
                return "Artist";
            case "release_date":
                return "Release Year";
            case "topic":
                return "Topic";
            default:
                return criteria;
        }
    }

    public void likeSong(User user, int songId) {
        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";
        String insertSql = "INSERT INTO liked_songs (user_id, song_id) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // بررسی وجود لایک
            checkStmt.setInt(1, user.getId());
            checkStmt.setInt(2, songId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("You've already liked this song!");
                return;
            }

            // اضافه کردن لایک
            insertStmt.setInt(1, user.getId());
            insertStmt.setInt(2, songId);
            insertStmt.executeUpdate();

            System.out.println("❤️ Song added to your liked songs!");

        } catch (SQLException e) {
            System.out.println("Error liking song: " + e.getMessage());
        }
    }

    public void unlikeSong(User user, int songId) {
        String sql = "DELETE FROM liked_songs WHERE user_id = ? AND song_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            stmt.setInt(2, songId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("💔 Song removed from your liked songs!");
            } else {
                System.out.println("Song not found in your liked songs!");
            }

        } catch (SQLException e) {
            System.out.println("Error unliking song: " + e.getMessage());
        }
    }

    public void showLikedSongs(User user) {
        // جایگزینی Text Block با String معمولی
        String sql = "SELECT s.id, s.artist_name, s.track_name, s.release_date, s.genre, s.len, s.topic " +
                "FROM liked_songs ls " +
                "JOIN songs s ON ls.song_id = s.id " +
                "WHERE ls.user_id = ? " +
                "ORDER BY ls.created_at DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n❤️ Your Liked Songs:");
            boolean hasLikedSongs = false;
            int count = 1;

            while (rs.next()) {
                System.out.printf("%d. %s - %s (%d) | Genre: %s | Length: %.0fs | Topic: %s%n",
                        count++,
                        rs.getString("artist_name"),
                        rs.getString("track_name"),
                        rs.getInt("release_date"),
                        rs.getString("genre"),
                        rs.getDouble("len"),
                        rs.getString("topic"));
                hasLikedSongs = true;
            }

            if (!hasLikedSongs) {
                System.out.println("You haven't liked any songs yet!");
            }

        } catch (SQLException e) {
            System.out.println("Error loading liked songs: " + e.getMessage());
        }
    }

    public void toggleLikeStatus(User user, Scanner scanner) {
        System.out.println("\n❤️ Like/Unlike Song");
        showAllSongs();

        System.out.print("Enter Song ID to like/unlike: ");
        int songId = Integer.parseInt(scanner.nextLine());

        String checkSql = "SELECT COUNT(*) FROM liked_songs WHERE user_id = ? AND song_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, user.getId());
            checkStmt.setInt(2, songId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // اگر لایک شده، دیسلایک کن
                unlikeSong(user, songId);
            } else {
                // اگر لایک نشده، لایک کن
                likeSong(user, songId);
            }

        } catch (SQLException e) {
            System.out.println("Error toggling like status: " + e.getMessage());
        }
    }

    // به‌روزرسانی متد showAllSongs برای نمایش وضعیت لایک
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

    public void checkLikedStatus(User user, List<Song> songs) {
        if (songs.isEmpty()) return;

        String sql = "SELECT song_id FROM liked_songs WHERE user_id = ? AND song_id IN (";
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < songs.size(); i++) {
            placeholders.append("?");
            if (i < songs.size() - 1) placeholders.append(",");
        }
        sql += placeholders + ")";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            for (int i = 0; i < songs.size(); i++) {
                stmt.setInt(i + 2, songs.get(i).getId());
            }

            ResultSet rs = stmt.executeQuery();
            Set<Integer> likedSongIds = new HashSet<>();
            while (rs.next()) {
                likedSongIds.add(rs.getInt("song_id"));
            }

            for (Song song : songs) {
                song.setLiked(likedSongIds.contains(song.getId()));
            }

        } catch (SQLException e) {
            System.out.println("Error checking liked status: " + e.getMessage());
        }
    }
}