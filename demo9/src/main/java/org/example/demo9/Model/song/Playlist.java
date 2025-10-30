package org.example.demo9.Model.song;

import java.util.*;

public class Playlist {
    private String name;
    private SongNode head;
    private int size;

    public Playlist(String name) {
        this.name = name;
        this.head = null;
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
        } else {
            SongNode currentNode = head;
            while (currentNode.next != null) {
                currentNode = currentNode.next;
            }
            currentNode.next = newNode;
        }
        size++;
    }

    public boolean removeSong(String trackName) {
        if (head == null) return false;
        if (head.data.getTrackName().equals(trackName)) {
            head = head.next;
            size--;
            return true;
        }
        SongNode prev = head;
        SongNode currentNode = head.next;
        while (currentNode != null) {
            if (currentNode.data.getTrackName().equalsIgnoreCase(trackName)) {
                prev.next = currentNode.next;
                size--;
                return true;
            }
            prev = currentNode;
            currentNode = currentNode.next;
        }
        return false;
    }

    public Song findSong(String trackName) {
        SongNode currentNode = head;
        while (currentNode != null) {
            if (currentNode.data.getTrackName().equalsIgnoreCase(trackName)) {
                return currentNode.data;
            }
            currentNode = currentNode.next;
        }
        return null;
    }

    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode currentNode = head;
        while (currentNode != null) {
            songs.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return songs;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    public void merge(Playlist playlist) {
        Set<Song> set = new HashSet<>();
        for (Song song : this.toList()) {
            set.add(song);
        }
        for (Song song : playlist.toList()) {
            if (!set.contains(song)) {
                this.addSong(song);
            }
        }
    }

    public static Playlist shuffleMerge(List<Playlist> lists, String newName) {
        Playlist result = new Playlist(newName);
        List<Song> pool = new ArrayList<>();
        Set<Song> seen = new HashSet<>();
        for (Playlist p : lists)
            for (Song s : p.toList())
                if (!seen.contains(s)) {
                    pool.add(s);
                    seen.add(s);
                }
        Collections.shuffle(pool);
        for (Song s : pool) result.addSong(s);
        return result;
    }

    public void sortBy(String criteria) {
        List<Song> list = toList();
        Comparator<Song> cmp;
        switch (criteria.toLowerCase()) {
            case "track name": cmp = Comparator.comparing(Song::getTrackName, String.CASE_INSENSITIVE_ORDER); break;
            case "artist name": cmp = Comparator.comparing(Song::getArtistName, String.CASE_INSENSITIVE_ORDER); break;
            case "release date": cmp = Comparator.comparing(Song::getReleaseDate, String.CASE_INSENSITIVE_ORDER); break;
            default: return;
        }
        list.sort(cmp);
        clear();
        for (Song s : list) addSong(s);
    }

    public Playlist filterBy(String criteria, String value) {
        Playlist out = new Playlist(this.name + "-filtered");
        for (Song s : toList()) {
            switch (criteria.toLowerCase()) {
                case "genre": if (s.getGenre().equalsIgnoreCase(value)) out.addSong(s); break;
                case "artist": if (s.getArtistName().equalsIgnoreCase(value)) out.addSong(s); break;
                case "year": if (s.getReleaseDate().equalsIgnoreCase(value)) out.addSong(s); break;
                case "topic": if (s.getTopic().equalsIgnoreCase(value)) out.addSong(s); break;
                default: break;
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

    public void play() {
        for (Song s : toList()) System.out.println(s);
    }

    public void shufflePlay() {
        List<Song> list = toList();
        Collections.shuffle(list);
        for (Song s : list) System.out.println(s);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Playlist '%s' (size=%d):\n", name, size));
        for (Song s : toList()) sb.append(" ").append(s.toString()).append('\n');
        return sb.toString();
    }
}
