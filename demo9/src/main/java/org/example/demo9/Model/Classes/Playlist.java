package org.example.demo9.Model.Classes;

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

    // اضافه کردن به انتهای لیست (اصلی‌ترین عملیات)
    public void addSong(SongNode song) {
        if (head == null) {
            head = song;
            tail = song;
        } else {
            tail.setNext(song);
            tail = song;
        }
        size++;
    }

    // اضافه کردن به ابتدای لیست
    public void addToFront(SongNode song) {
        if (head == null) {
            head = song;
            tail = song;
        } else {
            song.setNext(head);
            head = song;
        }
        size++;
    }

    // حذف از ابتدای لیست
    public SongNode removeFromFront() {
        if (head == null) return null;

        SongNode removed = head;
        head = head.getNext();
        if (head == null) {
            tail = null;
        }
        size--;
        return removed;
    }

    // حذف آهنگ بر اساس نام
    public boolean removeSong(String trackName) {
        if (head == null) return false;

        // اگر آهنگ اول باشد
        if (head.getTrackName().equals(trackName)) {
            head = head.getNext();
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        // جستجو در لیست
        SongNode current = head;
        while (current.getNext() != null) {
            if (current.getNext().getTrackName().equals(trackName)) {
                current.setNext(current.getNext().getNext());

                // اگر آخرین عنصر حذف شد
                if (current.getNext() == null) {
                    tail = current;
                }

                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    // جستجو در لیست
    public SongNode findSong(String trackName) {
        SongNode current = head;
        while (current != null) {
            if (current.getTrackName().equals(trackName)) {
                return current;
            }
            current = current.getNext();
        }
        return null;
    }

    // نمایش تمام آهنگ‌ها (پیمایش لیست)
    public void displaySongs() {
        SongNode current = head;
        int index = 1;
        while (current != null) {
            System.out.println(index + ". " + current);
            current = current.getNext();
            index++;
        }
    }

    // معکوس کردن لیست
    public void reverse() {
        SongNode prev = null;
        SongNode current = head;
        SongNode next = null;

        tail = head; // tail becomes the old head

        while (current != null) {
            next = current.getNext();
            current.setNext(prev);
            prev = current;
            current = next;
        }

        head = prev;
    }

    // ادغام دو لیست پیوندی
    public void merge(Playlist other) {
        if (other.head == null) return;

        if (this.head == null) {
            this.head = other.head;
            this.tail = other.tail;
        } else {
            this.tail.setNext(other.head);
            this.tail = other.tail;
        }
        this.size += other.size;

        // پاک کردن لیست دیگر
        other.head = null;
        other.tail = null;
        other.size = 0;
    }

    // فیلتر کردن بر اساس ژانر (ایجاد لیست جدید)
    public Playlist filterByGenre(String genre) {
        Playlist filtered = new Playlist(this.name + " - " + genre);
        SongNode current = head;

        while (current != null) {
            if (current.getGenre().equalsIgnoreCase(genre)) {
                filtered.addSong(new SongNode(
                        current.getArtistName(),
                        current.getTrackName(),
                        current.getReleaseDate(),
                        current.getGenre(),
                        current.getLen(),
                        current.getTopic()
                ));
            }
            current = current.getNext();
        }
        return filtered;
    }

    // مرتب‌سازی با استفاده از insertion sort (مناسب برای لیست پیوندی)
    public void sortByTrackName() {
        if (head == null || head.getNext() == null) return;

        SongNode sorted = null;
        SongNode current = head;

        while (current != null) {
            SongNode next = current.getNext();
            sorted = sortedInsert(sorted, current);
            current = next;
        }

        head = sorted;

        // به‌روزرسانی tail
        tail = sorted;
        while (tail != null && tail.getNext() != null) {
            tail = tail.getNext();
        }
    }

    private SongNode sortedInsert(SongNode sorted, SongNode newNode) {
        // اگر لیست مرتب شده خالی است یا باید در ابتدا قرار گیرد
        if (sorted == null || sorted.getTrackName().compareTo(newNode.getTrackName()) >= 0) {
            newNode.setNext(sorted);
            return newNode;
        }

        // پیدا کردن موقعیت مناسب
        SongNode current = sorted;
        while (current.getNext() != null &&
                current.getNext().getTrackName().compareTo(newNode.getTrackName()) < 0) {
            current = current.getNext();
        }

        newNode.setNext(current.getNext());
        current.setNext(newNode);
        return sorted;
    }

    // Getter methods
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SongNode getHead() { return head; }
    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }

    public SongNode getTail() { return tail; }
}