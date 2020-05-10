package ru.job4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class.getName());

    public static void main(String[] args) {
        LOG.error("Error");
        System.out.println("Hello World!");
    }
}
