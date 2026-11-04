/*
 * @test
 * @summary java.io.FileNotFoundException conformance: white-box structure + black-box
 *          behavior (constructors; real triggers from FileInputStream/FileOutputStream/
 *          RandomAccessFile for a missing path, a directory path and an unwritable target,
 *          message carrying the offending path).
 * @run main FileNotFoundExceptionTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class FileNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FileNotFoundException");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "exactly two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new FileNotFoundException().getMessage() == null, "no-arg message null");
        check("gone".equals(new FileNotFoundException("gone").getMessage()), "(String) message");

        // 2. trigger: reading a path that does not exist
        File missing = new File(System.getProperty("java.io.tmpdir"),
                                "fnfe-missing-" + System.nanoTime() + ".dat");
        try {
            new FileInputStream(missing);
            check(false, "FileInputStream on a missing file must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage() != null && fnfe.getMessage().contains(missing.getPath()),
                  "message contains the offending path");
        }
        try {
            new RandomAccessFile(missing, "r");
            check(false, "RandomAccessFile(r) on a missing file must throw");
        } catch (FileNotFoundException expected) {
            passed++;
        }

        // 3. trigger: opening a directory for reading
        File dir = new File(System.getProperty("java.io.tmpdir"));
        try {
            new FileInputStream(dir);
            check(false, "FileInputStream on a directory must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage() != null, "directory open reports a message");
        }

        // 4. trigger: creating a file inside a directory that does not exist
        File orphan = new File(missing.getParentFile(),
                               "fnfe-also-missing-" + System.nanoTime() + "/child.txt");
        try {
            new FileOutputStream(orphan);
            check(false, "FileOutputStream on an orphan path must throw");
        } catch (FileNotFoundException expected) {
            passed++;
        }

        // 5. contrast: an existing writable file opens fine, no exception
        File ok = File.createTempFile("fnfe-ok", ".dat");
        try (FileOutputStream fout = new FileOutputStream(ok)) {
            fout.write(1);
            passed++;
        } finally {
            ok.delete();
        }

        // 6. the exception is catchable as IOException and carries a usable toString
        try {
            new FileInputStream(missing);
        } catch (IOException io) {
            check(io instanceof FileNotFoundException && io.getMessage() != null,
                  "caught as IOException, still a FileNotFoundException");
            check(io.toString().startsWith("java.io.FileNotFoundException"),
                  "toString names the exception class");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
