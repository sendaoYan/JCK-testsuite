/*
 * @test
 * @summary JVMS 4.8: a class file whose magic number is not 0xCAFEBABE fails
 *          format checking, so loading it throws ClassFormatError.
 * @run main BadMagicClassFormatError
 */

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class BadMagicClassFormatError {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("badclass");

        // A class file with an incorrect magic number.
        byte[] bad = new byte[64];
        bad[0] = (byte) 0xDE; bad[1] = (byte) 0xAD;
        bad[2] = (byte) 0xBE; bad[3] = (byte) 0xEF;
        Files.write(dir.resolve("Bad.class"), bad);

        try {
            URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
            Class.forName("Bad", true, cl);
            throw new AssertionError("no ClassFormatError thrown");
        } catch (ClassFormatError e) {
            // expected per JVMS 4.8
        }

        System.out.println("PASS");
    }
}
