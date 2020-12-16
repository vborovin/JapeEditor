package com.vborovin.japeparser.debuglog;

import java.util.ArrayList;

public class DebugLog {
    private ArrayList<DebugLogEntry> logEntries;
    private int blockShift = 0;

    private int currentOffset = 0;

    public DebugLog() {
        logEntries = new ArrayList<>();
    }

    public void append(String content, int offset, int length, int type) {
        String tabs = new String(new char[blockShift]).replace('\0', '\t');
        content = tabs + content + "\n";
        int selfLength = content.length();
        logEntries.add(new DebugLogEntry(content, offset, length, currentOffset, selfLength, type));
        currentOffset += content.length();
    }

    public void appendBlockBegin(String content, int offset, int length, int type) {
        String tabs = new String(new char[blockShift]).replace('\0', '\t');
        content = tabs + content + " {{{" + "\n";
        int selfLength = content.length();
        logEntries.add(new DebugLogEntry(content, offset, length, currentOffset, selfLength, type));
        currentOffset += content.length();
        blockShift++;
    }

    public void appendBlockEnd() {
        blockShift--;
        String tabs = new String(new char[blockShift]).replace('\0', '\t');
        tabs = tabs + "}}}" + "\n";
        int selfLength = tabs.length();
        logEntries.add(new DebugLogEntry(tabs, currentOffset, selfLength));
        currentOffset += tabs.length();
    }

    public void clear() {
        logEntries.clear();
        currentOffset = 0;
    }

    public DebugLogEntry getEntryAtOffset(int offset) {
        for (DebugLogEntry entry : logEntries) {
            int selfOffset = entry.getSelfOffset();
            int selfLength = entry.getSelfLength();
            if (selfOffset <= offset && selfOffset + selfLength >= offset) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DebugLogEntry entry : logEntries) {
            sb.append(entry);
        }

        return sb.toString();
    }
}