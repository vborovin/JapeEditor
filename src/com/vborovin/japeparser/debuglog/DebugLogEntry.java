package com.vborovin.japeparser.debuglog;

public class DebugLogEntry {

    public static int TYPE_OTHER = 0;
    public static int TYPE_PATTERN = 1;

    private String content;
    private int offset;
    private int length;

    private int type;
    private int selfOffset;
    private int selfLength;

    DebugLogEntry(String content, int entryOffset, int selfLength) {
        this.content = content;
        this.selfLength = selfLength;
        this.selfOffset = entryOffset;
    }

    DebugLogEntry(String content, int offset, int length, int entryOffset, int selfLength, int type) {
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.content = content;
        this.selfLength = selfLength;
        this.selfOffset = entryOffset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSelfOffset() {
        return selfOffset;
    }

    public void setSelfOffset(int entryOffset) {
        this.selfOffset = entryOffset;
    }

    public int getSelfLength() {
        return selfLength;
    }

    public void setSelfLength(int selfLength) {
        this.selfLength = selfLength;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}