package org.example.demo9.Model.Classes;

public class SongNode {
    public org.example.demo9.Model.Classes.Song data;
    public SongNode next;
    public SongNode prev;

    public SongNode(org.example.demo9.Model.Classes.Song data){
        this.data = data;
        this.next = null;
        this.prev = null;
    }
}
