package org.example.demo9;

import org.example.demo9.Controller.PlaylistController;
import org.example.demo9.Model.song.Playlist;
import org.example.demo9.Model.song.PlaylistManager;
import org.example.demo9.View.CommandView;

public class Main {
    public static void main(String[] args) {
        PlaylistManager manager = new PlaylistManager();
        PlaylistController controller = new PlaylistController(manager);
        CommandView view = new CommandView(manager);

        System.out.println(view.showMenu());

        while (true) {
            String input = view.getUserCommand().trim();
            if (input.equalsIgnoreCase("exit"))
                break;

            try {
                String[] parts = input.split(" ", 2);
                String command = parts[0].toLowerCase();

                switch (command) {
                    case "create": {
                        controller.handleCreatePlaylist(parts[1]);
                        view.displayMessage("Playlist created: " + parts[1]);
                        break;
                    }
                    case "load": {
                        String[] args2 = parts[1].split(" ");
                        controller.loadSongsFromCSV(args2[0], args2[1]);
                        view.displayMessage("Loaded songs from " + args2[0]);
                        break;
                    }
                    case "list": {
                        for (Playlist p : manager.allPlaylists()) {
                            view.displayMessage(p.getName());
                        }
                        break;
                    }
                    case "show": {
                        Playlist p = manager.getPlaylist(parts[1]);
                        view.displayPlaylist(p);
                        break;
                    }
                    case "play": {
                        String[] args2 = parts[1].split(" ");
                        String name = args2[0];
                        boolean shuffle = args2.length > 1 && args2[1].equalsIgnoreCase("shuffle");
                        controller.handlePlay(name, shuffle);
                        break;
                    }
                    case "sort": {
                        String[] args2 = parts[1].split(" ");
                        controller.handleSort(args2[0], args2[1]);
                        view.displayMessage("Sorted playlist: " + args2[0]);
                        break;
                    }
                    case "filter": {
                        String[] args2 = parts[1].split(" ");
                        Playlist filtered = controller.handleFilter(args2[0], args2[1], args2[2]);
                        view.displayPlaylist(filtered);
                        break;
                    }
                    case "like": {
                        String[] args2 = parts[1].split(" ");
                        controller.handleLike(args2[0], args2[1]);
                        view.displayMessage("Liked " + args2[1]);
                        break;
                    }
                    case "unlike": {
                        String[] args2 = parts[1].split(" ");
                        controller.handleUnlike(args2[0], args2[1]);
                        view.displayMessage("Unliked " + args2[1]);
                        break;
                    }
                    case "liked": {
                        Playlist liked = manager.getLikedSongsPlaylist();
                        view.displayPlaylist(liked);
                        break;
                    }
                    default:
                        view.displayError("Unknown command: " + command);
                }

            } catch (Exception e) {
                view.displayError("Error: " + e.getMessage());
            }
        }
    }
}
