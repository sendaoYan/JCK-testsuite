/*
 * @test
 * @summary JVMS 5.3.5: deriving a class from a class file whose major version
 *          is higher than the JVM supports is rejected with
 *          UnsupportedClassVersionError.
 * @run main UnsupportedClassVersion
 */

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnsupportedClassVersion {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("toonew");

        // A minimal class file: valid magic, then major version 99.
        byte[] b = new byte[64];
        b[0] = (byte) 0xCA; b[1] = (byte) 0xFE;
        b[2] = (byte) 0xBA; b[3] = (byte) 0xBE;
        b[4] = 0; b[5] = 0;       // minor_version = 0
        b[6] = 0; b[7] = (byte) 99; // major_version = 99
        Files.write(dir.resolve("TooNew.class"), b);

        boolean threw = false;
        try {
            URLClassLoader cl = new URLClassLoader(
                    new URL[]{dir.toUri().toURL()}, null);
            Class.forName("TooNew", true, cl);
        } catch (UnsupportedClassVersionError e) {
            threw = true;
        }
        check(threw, "too-high major version rejected with UnsupportedClassVersionError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
