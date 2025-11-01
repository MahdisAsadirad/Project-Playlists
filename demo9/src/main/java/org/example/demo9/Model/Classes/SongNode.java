package org.example.demo9.Model.Classes;

public class SongNode {
    private Song data;
    private SongNode next;
    private SongNode prev;

    public SongNode(org.example.demo9.Model.Classes.Song data){
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    public Song getData() {
        return data;
    }

    public void setData(Song data) {
        this.data = data;
    }

    public SongNode getNext() {
        return next;
    }

    public void setNext(SongNode next) {
        this.next = next;
    }

    public SongNode getPrev() {
        return prev;
    }

    public void setPrev(SongNode prev) {
        this.prev = prev;
    }
}
