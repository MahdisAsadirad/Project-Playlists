package org.example.demo9.Model.song;

public class SongNode {
    public Song data;
    public SongNode next;
    public SongNode prev;

    public SongNode(Song data){
        this.data = data;
        this.next = null;
        this.prev = null;
    }
}
