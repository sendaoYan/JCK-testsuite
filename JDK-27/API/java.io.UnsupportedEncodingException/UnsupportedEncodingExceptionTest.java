/*
 * @test
 * @summary java.io.UnsupportedEncodingException conformance: white-box structure +
 *          black-box behavior (constructors; real triggers: String.getBytes,
 *          new String(bytes, charsetName), InputStreamReader/OutputStreamWriter and
 *          ByteArray*Stream constructors with an unknown charset name; the message is
 *          the offending charset name).
 * @run main UnsupportedEncodingExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

public class UnsupportedEncodingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.UnsupportedEncodingException");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "two public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String bogus = "definitely-not-a-charset-" + System.nanoTime();

        // 1. constructors
        check(new UnsupportedEncodingException().getMessage() == null, "no-arg message null");
        check(bogus.equals(new UnsupportedEncodingException(bogus).getMessage()),
              "(String) message is the charset name by convention");

        // 2. trigger: String.getBytes(charsetName)
        try {
            "x".getBytes(bogus);
            check(false, "getBytes with an unknown charset must throw");
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "message names the charset: " + uee.getMessage());
        }

        // 3. trigger: new String(bytes, charsetName)
        try {
            new String(new byte[1], bogus);
            check(false, "String ctor with an unknown charset must throw");
        } catch (UnsupportedEncodingException expected) {
            check(bogus.equals(expected.getMessage()), "message names the charset");
        }

        // 4. trigger: reader and writer constructors
        try {
            new InputStreamReader(new ByteArrayInputStream(new byte[1]), bogus);
            check(false, "InputStreamReader with an unknown charset must throw");
        } catch (UnsupportedEncodingException expected) {
            passed++;
        }
        try {
            new OutputStreamWriter(new ByteArrayOutputStream(), bogus);
            check(false, "OutputStreamWriter with an unknown charset must throw");
        } catch (UnsupportedEncodingException expected) {
            passed++;
        }

        // 5. contrast: a legal charset name works (by-name API remains supported)
        byte[] b = "héllo".getBytes("UTF-8");
        check(new String(b, "UTF-8").equals("héllo"), "valid name round-trips");
        check(Charset.forName("UTF-8").newDecoder() != null, "Charset path unaffected");
        check(new String(new byte[]{'o', 'k'}, "US-ASCII").equals("ok"), "US-ASCII path");

        // 6. caught as IOException; serializes keeping its message
        try {
            throw new UnsupportedEncodingException("m");
        } catch (IOException io) {
            check(io instanceof UnsupportedEncodingException, "catchable as IOException");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new UnsupportedEncodingException("kept"));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("kept".equals(((UnsupportedEncodingException) ois.readObject()).getMessage()),
                  "message survives serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
