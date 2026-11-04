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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        FileNotFoundException e = new FileNotFoundException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.FileNotFoundException: m"),
              "toString = FQCN + \": \" + message");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        StackTraceElement[] st = {new StackTraceElement("F", "open", "F.java", 2)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1 && "open".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new java.nio.file.NoSuchFileException("/x/y");
        check(new FileNotFoundException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new FileNotFoundException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new FileNotFoundException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        FileNotFoundException sup = new FileNotFoundException("s");
        sup.addSuppressed(new Exception("x"));
        check(sup.getSuppressed().length == 1, "suppressed kept");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(FileNotFoundException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(FileNotFoundException.class).getSerialVersionUID()
                  == -897856973823710492L, "serialVersionUID == -897856973823710492");
        FileNotFoundException r =
            (FileNotFoundException) back(serOf(new FileNotFoundException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.FileNotFoundException: kept"),
              "toString survives round-trip");
        check(back(serOf(new FileNotFoundException())) instanceof FileNotFoundException
                  && ((FileNotFoundException) back(serOf(new FileNotFoundException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        FileNotFoundException chained = new FileNotFoundException("w");
        chained.initCause(new RuntimeException("r"));
        FileNotFoundException rc = (FileNotFoundException) back(serOf(chained));
        check("r".equals(rc.getCause().getMessage()), "cause survives round-trip");
    }

    // ---- message content contract: path + errno suffix (实测双侧一致) ----
    static void testMessageContent() throws Exception {
        File base = new File(System.getProperty("java.io.tmpdir"));
        File missing = new File(base, "fnfe-deep-" + System.nanoTime() + ".dat");
        try {
            new FileInputStream(missing);
            check(false, "missing file must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage().startsWith(missing.getPath()),
                  "message starts with the offending path");
            check(fnfe.getMessage().contains("(No such file or directory)"),
                  "missing file errno suffix: " + fnfe.getMessage());
            check(fnfe.getMessage().equals(fnfe.getLocalizedMessage()),
                  "getLocalizedMessage identical for platform messages");
            check(fnfe.toString().startsWith("java.io.FileNotFoundException: " + missing.getPath()),
                  "toString carries path");
        }
        // directory opened for reading reports (Is a directory)
        try {
            new FileInputStream(base);
            check(false, "directory open must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage().contains("(Is a directory)"),
                  "directory errno suffix: " + fnfe.getMessage());
        }
        // write into a missing parent directory
        File orphan = new File(base, "fnfe-orphan-" + System.nanoTime() + "/child.txt");
        try {
            new FileOutputStream(orphan);
            check(false, "orphan path write must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage().contains(orphan.getPath())
                      && fnfe.getMessage().contains("(No such file or directory)"),
                  "orphan write carries path + errno: " + fnfe.getMessage());
        }
        // RandomAccessFile "r" on missing path reuses the same exception type
        try {
            new RandomAccessFile(missing, "r");
            check(false, "RAF(r) missing must throw");
        } catch (FileNotFoundException fnfe) {
            check(fnfe.getMessage().contains("(No such file or directory)"),
                  "RAF errno suffix: " + fnfe.getMessage());
        }
        // contrast: "rw" creates the file instead of throwing
        File created = new File(base, "fnfe-rw-" + System.nanoTime() + ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(created, "rw")) {
            raf.write(1);
            passed++;
        } finally {
            created.delete();
        }
        // constructed message is verbatim, no errno appended
        check("custom".equals(new FileNotFoundException("custom").getMessage()),
              "constructed message stays verbatim");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testMessageContent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

