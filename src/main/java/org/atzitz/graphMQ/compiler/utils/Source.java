package org.atzitz.graphMQ.compiler.utils;

import lombok.Getter;

public class Source {
    private final @Getter String initial;

    private int crow = 0;
    private int ccol = 0;
    private int cacheRow;
    private int cacheCol;
    private String value;
    private String cache;

    public Source(String initial) {
        this.initial = initial;
        this.value = initial;
        this.cache = "";
    }

    public char consume() throws ArrayIndexOutOfBoundsException {
        if (value.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        char c = value.charAt(0);
        if (c == '\n') {
            ccol = 0;
            crow++;
        } else {
            ccol++;
        }

        value = value.substring(1);
        return c;
    }

    public String save() {
        if (cacheEmpty()) {
            cacheRow = crow;
            cacheCol = ccol;
        }

        cache += seek();
        return cache;
    }

    public String clearCache() {
        var saved = this.cache;
        this.cache = "";
        return saved;
    }

    public boolean empty() {
        return this.value.isEmpty();
    }

    public boolean cacheEmpty() {
        return this.cache.isEmpty();
    }

    public char seek() {
        return value.charAt(0);
    }

    public char seekNext() {
        return value.charAt(1);
    }

    public int find(char c) {
        return value.indexOf(c, 1);
    }

    public String slice(int end) {
        return value.substring(1, end);
    }

    public void jump(int index) {
        value = value.substring(index);
    }

    public int length() {
        return value.length();
    }

    public void push(char c) {
        value = c + value;
    }

    public int ccol() {
        return ccol;
    }

    public int crow() {
        return crow;
    }

    public int cacheRow() {
        return cacheRow;
    }

    public int cacheCol() {
        return cacheCol;
    }
}
