package org.example.demo9.View;

import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.PlaylistManager;
import org.example.demo9.Model.song.Song;

import java.util.Scanner;

public class CommandView {
    private Scanner scanner = new Scanner(System.in);
    private PlaylistManager manager;


    public CommandView(PlaylistManager manager) {
        this.manager = manager;
    }


    public String showMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("*-*-*-* Playlist *-*-*-*\n\n");
        sb.append("Commands:\n");
        sb.append("  create <name>                         - Create a new playlist\n");
        sb.append("  delete <name>                         - Delete a playlist\n");
        sb.append("  load <csv> <target>                   - Load songs from CSV into a playlist\n");
        sb.append("  list                                  - List all playlists\n");
        sb.append("  show <name>                            - Show songs in a playlist\n");
        sb.append("  add <name> <artist>|<track>|<year>|<genre>|<len>|<topic>  - Add a song\n");
        sb.append("  remove <name> <track>                 - Remove a song from a playlist\n");
        sb.append("  merge <p1> <p2> <new>                 - Merge two playlists into a new one\n");
        sb.append("  shufmerge <p1,p2,...> <new>           - Shuffle merge multiple playlists\n");
        sb.append("  sort <name> <criteria>                - Sort a playlist (track name, artist, release date)\n");
        sb.append("  filter <name> <criteria> <value>      - Filter songs by genre, artist, year, topic\n");
        sb.append("  like <name> <track>                    - Like a song\n");
        sb.append("  unlike <name> <track>                  - Unlike a song\n");
        sb.append("  liked                                  - Show liked songs\n");
        sb.append("  play <name> [shuffle]                  - Play a playlist (shuffle optional)\n");
        sb.append("  exit                                   - Exit the program\n");
        return sb.toString();
    }



    public String getUserCommand() {
        System.out.print("cmd> ");
        return scanner.nextLine();
    }


    public void displayPlaylist(Playlist p) {
        if (p == null) System.out.println("(null)");
        else System.out.println(p);
    }


    public void displaySong(Song s) {
        if (s == null) System.out.println("(song not found)");
        else System.out.println(s);
    }


    public void displayMessage(String msg) {
        System.out.println(msg);
    }

    public void displayError(String err) {
        System.err.println(err);
    }
}
