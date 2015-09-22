package me.madsbf.launcher;

public class PackageChange {

    public final int uid;
    public final String event;

    public PackageChange(int uid, String event) {
        this.uid = uid;
        this.event = event;
    }
}
