package org.example.demo9.Model.song;

import java.util.*;

public class Playlist {
    private String name;
    private SongNode head;
    private SongNode tail;
    private int size;

    public Playlist(String name) {
        this.name = name;
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }


    public void addSong(Song song) {
        SongNode newNode = new SongNode(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }


    public boolean removeSong(String trackName) {
        SongNode current = head;
        while (current != null) {
            if (current.data.getTrackName().equalsIgnoreCase(trackName)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next; // حذف اولین نود

                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev; // حذف آخرین نود

                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }


    public Song findSong(String trackName) {
        SongNode current = head;
        while (current != null) {
            if (current.data.getTrackName().equalsIgnoreCase(trackName)) return current.data;
            current = current.next;
        }
        return null;
    }


    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode current = head;
        while (current != null) {
            songs.add(current.data);
            current = current.next;
        }
        return songs;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }


    public void merge(Playlist other) {
        SongNode current = other.head;
        while (current != null) {
            if (findSong(current.data.getTrackName()) == null) addSong(current.data);
            current = current.next;
        }
    }


    public static Playlist shuffleMerge(List<Playlist> lists, String newName) {
        Playlist result = new Playlist(newName);
        List<Song> pool = new ArrayList<>();
        Set<Song> seen = new HashSet<>();
        for (Playlist p : lists) {
            for (Song s : p.toList()) {
                if (!seen.contains(s)) {
                    pool.add(s);
                    seen.add(s);
                }
            }
        }
        Collections.shuffle(pool);
        for (Song s : pool) result.addSong(s);
        return result;
    }


    public void sortBy(String criteria) {
        List<Song> list = toList();
        Comparator<Song> cmp;
        switch (criteria.toLowerCase()) {
            case "track name":
            case "track_name":
                cmp = Comparator.comparing(Song::getTrackName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "artist name":
            case "artist_name":
                cmp = Comparator.comparing(Song::getArtistName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "release date":
            case "release_date":
                cmp = Comparator.comparing(Song::getReleaseDate, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                return;
        }
        list.sort(cmp);
        clear();
        for (Song s : list) addSong(s);
    }


    public Playlist filterBy(String criteria, String value) {
        Playlist out = new Playlist(this.name + "-filtered");
        for (Song s : toList()) {
            switch (criteria.toLowerCase()) {
                case "genre":
                    if (s.getGenre().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "artist":
                    if (s.getArtistName().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "year":
                    if (s.getReleaseDate().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                case "topic":
                    if (s.getTopic().equalsIgnoreCase(value)) out.addSong(s);
                    break;
                default:
                    break;
            }
        }
        return out;
    }


    public void likeSong(String trackName) {
        Song s = findSong(trackName);
        if (s != null) s.setLiked(true);
    }

    public void unlikeSong(String trackName) {
        Song s = findSong(trackName);
        if (s != null) s.setLiked(false);
    }


    public String play() {
        StringBuilder sb = new StringBuilder();
        SongNode current = head;
        while (current != null) {
            sb.append(current.data).append("\n");
            current = current.next;
        }
        return sb.toString();
    }


    public String shufflePlay() {
        List<Song> list = toList();
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (Song s : list) sb.append(s).append("\n");
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🎶 Playlist: '%s' | Total Songs: %d 🎶\n", name, size));
        sb.append("------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-25s %-30s %-6s %-10s %-6s %-10s\n",
                "Artist", "Track", "Year", "Genre", "     Len(s)", "Topic"));
        sb.append("------------------------------------------------------------------------------------\n");

        SongNode current = head;
        while (current != null) {
            Song s = current.data;
            sb.append(String.format("%-25s %-30s %-6s %-15s %-6.0f %-40s\n",
                    s.getArtistName(),
                    s.getTrackName(),
                    s.getReleaseDate(),
                    s.getGenre(),
                    s.getLength(),
                    s.getTopic()));
            current = current.next;
        }

        sb.append("------------------------------------------------------------------------------------\n");
        return sb.toString();
    }



    public String playBackward() {
        StringBuilder sb = new StringBuilder();
        SongNode current = tail;
        while (current != null) {
            sb.append(current.data).append("\n");
            current = current.prev;
        }
        return sb.toString();
    }
}
