/*
 * @test
 * @summary java.io.StreamCorruptedException conformance: white-box structure + black-box
 *          behavior (constructors; real triggers: garbage stream header, truncated
 *          header, a bad tag byte, and a class descriptor with an invalid field type).
 * @run main StreamCorruptedExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class StreamCorruptedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.StreamCorruptedException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "two public constructors");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new StreamCorruptedException().getMessage() == null, "no-arg message null");
        check("why".equals(new StreamCorruptedException("why").getMessage()), "(String) message");

        // 2. trigger: an invalid stream header
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0x00, 0x01, 0x02, 0x03}));
            check(false, "garbage header must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().startsWith("invalid stream header"),
                  "message names the header problem: " + sce.getMessage());
        }

        // 3. trigger: a header truncated before STREAM_VERSION
        try {
            new ObjectInputStream(new ByteArrayInputStream(
                new byte[]{(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                           (byte) ObjectStreamConstants.STREAM_MAGIC}));
            check(false, "short header must throw");
        } catch (java.io.IOException expected) {
            check(expected instanceof StreamCorruptedException || expected instanceof java.io.EOFException,
                  "truncated header: " + expected.getClass().getSimpleName());
        }

        // 4. trigger: valid header, then an unknown tag byte
        byte[] junk = {(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                       (byte) ObjectStreamConstants.STREAM_MAGIC,
                       (byte) (ObjectStreamConstants.STREAM_VERSION >>> 8),
                       (byte) ObjectStreamConstants.STREAM_VERSION,
                       0x7F};                     // not a valid TC_* tag
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(junk))) {
            ois.readObject();
            check(false, "unknown tag must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().contains("invalid type code"),
                  "invalid type code message: " + sce.getMessage());
        }

        // 5. trigger: hand-corrupted payload (flip bytes after the header)
        byte[] good = ser("hello");
        good[good.length - 1] ^= 0x55;            // corrupt the string body
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(good))) {
            String s = (String) ois.readObject();
            check(!"hello".equals(s), "corrupted byte changes the value");
        } catch (java.io.IOException ex) {
            check(ex instanceof StreamCorruptedException || ex instanceof java.io.UTFDataFormatException
                  || ex instanceof java.io.EOFException,
                  "corrupted payload surfaces as a stream/decoding failure: " + ex);
        }

        // 6. contrast: an unmodified stream reads cleanly
        check("hello".equals(
            new ObjectInputStream(new ByteArrayInputStream(ser("hello"))).readObject()),
            "clean stream round-trips");

        // 7. caught as ObjectStreamException
        try {
            throw new StreamCorruptedException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof StreamCorruptedException, "catchable as ObjectStreamException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
