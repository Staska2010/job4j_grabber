package ru.job4j.quartz;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class AllertRabbitTest {
    @Test
    public void test() {
        //Path file;
        InputStream is = AllertRabbitTest.class.getClassLoader().getResourceAsStream("rabbit.properties");
        byte[] chars = new byte[256];

        try {
            is.read(chars);
            Path path = Path.of("rabbit.properties");
            System.out.println(path.toAbsolutePath().toString());
            FileReader file = new FileReader("rabbit.properties");
            byte[] array = new byte[256];
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        System.out.println("Hello");
    }
}
