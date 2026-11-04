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
import java.io.ObjectStreamClass;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
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
        UnsupportedEncodingException e = new UnsupportedEncodingException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.UnsupportedEncodingException: m"),
              "toString = FQCN + \": \" + message");
        check(new UnsupportedEncodingException().toString()
                  .equals("java.io.UnsupportedEncodingException"),
              "no-arg toString carries no colon");
        check(new UnsupportedEncodingException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new UnsupportedEncodingException("").getMessage().isEmpty(),
              "empty message distinct from null (empty charset name case)");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("S", "getBytes", "S.java", 7)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "getBytes".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new UnsupportedEncodingException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new UnsupportedEncodingException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new UnsupportedEncodingException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        UnsupportedEncodingException sup = new UnsupportedEncodingException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in order");
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
        check(UnsupportedEncodingException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(UnsupportedEncodingException.class).getSerialVersionUID()
                  == -4274276298326136670L, "serialVersionUID == -4274276298326136670");
        UnsupportedEncodingException r =
            (UnsupportedEncodingException) back(serOf(new UnsupportedEncodingException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.UnsupportedEncodingException: kept"),
              "toString survives round-trip");
        check(((UnsupportedEncodingException)
                   back(serOf(new UnsupportedEncodingException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        UnsupportedEncodingException chained = new UnsupportedEncodingException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((UnsupportedEncodingException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
        UnsupportedEncodingException sup = new UnsupportedEncodingException("s");
        sup.addSuppressed(new IllegalStateException("z"));
        check(((UnsupportedEncodingException) back(serOf(sup))).getSuppressed().length == 1,
              "suppressed survive round-trip");
    }

    // ---- the message is the charset name on EVERY java.io by-name surface (实测双侧一致) ----
    static void testTriggerSurface() throws Exception {
        String bogus = "jck-no-such-charset-987654";
        // PrintStream(OutputStream, boolean, String)
        try {
            new PrintStream(new ByteArrayOutputStream(), true, bogus);
            check(false, "PrintStream with an unknown charset must throw");
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "PrintStream names the charset");
        }
        // PrintWriter(String fileName, String charsetName)
        try {
            new PrintWriter(System.getProperty("java.io.tmpdir") + "/jck-uee-probe.txt", bogus);
            check(false, "PrintWriter with an unknown charset must throw");
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "PrintWriter names the charset");
        }
        // ByteArrayOutputStream.toString(String)
        try {
            new ByteArrayOutputStream().toString(bogus);
            check(false, "BAOS.toString with an unknown charset must throw");
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "BAOS.toString names the charset");
        }
        // InputStreamReader / OutputStreamWriter also carry the bare name
        try {
            new InputStreamReader(new ByteArrayInputStream(new byte[1]), bogus);
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "InputStreamReader names the charset");
        }
        try {
            new OutputStreamWriter(new ByteArrayOutputStream(), bogus);
        } catch (UnsupportedEncodingException uee) {
            check(bogus.equals(uee.getMessage()), "OutputStreamWriter names the charset");
        }
        // a near-miss REAL name is still refused, verbatim
        try {
            "x".getBytes("UTF-88");
        } catch (UnsupportedEncodingException uee) {
            check("UTF-88".equals(uee.getMessage()),
                  "near-miss name reported verbatim: " + uee.getMessage());
        }
        // empty string is an unsupported charset with an empty diagnostic (not null)
        try {
            "x".getBytes("");
        } catch (UnsupportedEncodingException uee) {
            check(uee.getMessage() != null && uee.getMessage().isEmpty(),
                  "empty name gives an empty message");
        }
        // null name is a NullPointerException, not a charset failure (实测)
        try {
            "x".getBytes((String) null);
            check(false, "null charset name must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new String(new byte[1], (String) null);
            check(false, "null charset name in String ctor must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        // case-insensitive alias families all resolve, by name and by Charset
        check(new String(new byte[]{'h', 'i'}, "utf8").equals("hi"),
              "historical alias utf8 works");
        check("ab".getBytes("UnicodeLittle").length > 0, "UnicodeLittle alias works");
        check(new String("ab".getBytes("iso-8859-1"), "ISO8859_1").equals("ab"),
              "mixed-case historical alias works");
        check(Charset.isSupported("UTF-8"), "Charset.isSupported agrees for legal names");
        check(!Charset.isSupported(bogus), "Charset.isSupported agrees for the bogus name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testTriggerSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
