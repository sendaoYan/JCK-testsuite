/*
 * @test
 * @summary java.net.URLClassLoader conformance: white-box structure + black-box behavior.
 * @run main URLClassLoaderTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class URLClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLClassLoader");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(ClassLoader.class.isAssignableFrom(c), "extends ClassLoader");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(c.getConstructor(URL[].class, ClassLoader.class) != null, "ctor(URL[],ClassLoader)");
        check(c.getMethod("getURLs") != null, "getURLs");
        check(c.getMethod("close") != null, "close");
        check(c.getMethod("newInstance", URL[].class) != null, "newInstance");
    }

    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-ucl");
        Files.write(dir.resolve("hello.txt"), "world".getBytes(StandardCharsets.UTF_8));
        URL url = dir.toUri().toURL();

        URLClassLoader ucl = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
        try {
            URL[] urls = ucl.getURLs();
            check(urls.length == 1, "one url");
            check(urls[0].equals(url), "url retained");

            try (InputStream is = ucl.getResourceAsStream("hello.txt")) {
                check(is != null, "resource stream non-null");
                check(new String(is.readAllBytes(), StandardCharsets.UTF_8).equals("world"), "resource content");
            }

            URL r = ucl.findResource("hello.txt");
            check(r != null && r.equals(new URL(url, "hello.txt")), "findResource");

            Enumeration<URL> rs = ucl.findResources("hello.txt");
            check(rs.hasMoreElements(), "findResources has element");
        } finally {
            ucl.close();
        }

        URLClassLoader u2 = URLClassLoader.newInstance(new URL[]{url});
        check(u2.getURLs().length == 1, "newInstance urls");
        u2.close();

        Files.deleteIfExists(dir.resolve("hello.txt"));
        Files.deleteIfExists(dir);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
