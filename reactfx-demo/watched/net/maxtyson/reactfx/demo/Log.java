package net.maxtyson.reactfx.demo;

import javafx.scene.control.ListView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log extends ListView<String> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public Log() { setPrefHeight(260); }

    public void log(String msg) {
        getItems().add(LocalTime.now().format(TIME_FORMATTER) + "  " + msg);

        if (getItems().size() > 300)
            getItems().remove(0);

        scrollTo(getItems().size() - 1);
    }
}