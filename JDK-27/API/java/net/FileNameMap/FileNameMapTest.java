/*
 * @test
 * @summary java.net.FileNameMap conformance: white-box structure + black-box behavior.
 * @run main FileNameMapTest
 */

import java.lang.reflect.*;
import java.net.*;

public class FileNameMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.FileNameMap");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("getContentTypeFor", String.class) != null, "getContentTypeFor");
    }

    static void testBlackBox() throws Exception {
        FileNameMap map = new FileNameMap() {
            public String getContentTypeFor(String fileName) {
                if (fileName.endsWith(".txt")) return "text/plain";
                if (fileName.endsWith(".html")) return "text/html";
                return null;
            }
        };
        check(map.getContentTypeFor("readme.txt").equals("text/plain"), "txt");
        check(map.getContentTypeFor("index.html").equals("text/html"), "html");
        check(map.getContentTypeFor("unknown.bin") == null, "unknown null");

        // URLConnection.getFileNameMap() returns a JDK-provided implementation
        FileNameMap jdk = URLConnection.getFileNameMap();
        check(jdk != null, "getFileNameMap non-null");
        check(jdk.getContentTypeFor("x.txt") != null, "jdk maps .txt");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
