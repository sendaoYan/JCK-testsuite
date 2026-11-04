/*
 * @test
 * @summary java.nio.charset.UnsupportedCharsetException conformance: white-box structure + black-box name accessor.
 * @run main UnsupportedCharsetExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

public class UnsupportedCharsetExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.UnsupportedCharsetException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getConstructor(String.class) != null, "String constructor present");
    }

    static void testBlackBox() {
        UnsupportedCharsetException e = new UnsupportedCharsetException("no-such-charset");
        check("no-such-charset".equals(e.getCharsetName()), "getCharsetName");
        check("no-such-charset".equals(e.getMessage()), "getMessage equals charset name");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        check(e.getClass().getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        expect(UnsupportedCharsetException.class, () -> Charset.forName("definitely-not-a-real-charset"),
                "forName throws UnsupportedCharsetException");
    }

    /** Runs r, expects UnsupportedCharsetException, returns it (never null on success). */
    static UnsupportedCharsetException captureUce(Runnable r, String msg) {
        try {
            r.run();
        } catch (UnsupportedCharsetException t) {
            passed++;
            return t;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBoxSurface() throws Exception {
        Class<?> c = UnsupportedCharsetException.class;
        check(Serializable.class.isAssignableFrom(c), "Serializable via RuntimeException");
        // sole accessor beyond Throwable state
        check(c.getDeclaredMethods().length == 1
                && "getCharsetName".equals(c.getDeclaredMethods()[0].getName())
                && c.getDeclaredMethods()[0].getReturnType() == String.class,
                "single declared method getCharsetName() returning String");
    }

    // Constructor matrix: message mirrors the requested name verbatim; a null name is
    // stored as null but rendered as the literal string "null" by String.valueOf.
    static void testCtorMatrix() {
        UnsupportedCharsetException e = new UnsupportedCharsetException("no-such-charset");
        check("no-such-charset".equals(e.getCharsetName()), "getCharsetName round trip");
        check("no-such-charset".equals(e.getMessage()), "getMessage mirrors charset name");
        check("java.nio.charset.UnsupportedCharsetException: no-such-charset".equals(e.toString()),
                "toString == className: name");

        UnsupportedCharsetException nullName = new UnsupportedCharsetException(null);
        check(nullName.getCharsetName() == null, "null name: getCharsetName() returns null, no throw");
        check("null".equals(nullName.getMessage()), "null name: getMessage() is literal \"null\"");

        UnsupportedCharsetException emptyName = new UnsupportedCharsetException("");
        check("".equals(emptyName.getCharsetName()), "empty name preserved verbatim");
        check("".equals(emptyName.getMessage()), "empty name message empty");

        check(e instanceof RuntimeException, "is RuntimeException");
    }

    // Real triggers via Charset.forName/isSupported: only syntactically legal names reach
    // the lookup stage, so this exception always carries a legal (but unknown) name.
    static void testForNameTriggers() {
        expect(IllegalArgumentException.class, () -> Charset.forName(null),
                "forName(null) -> IllegalArgumentException, never UCE");
        for (String unknown : new String[]{"no-such-charset-xyz", "1ABC", "A+B"}) {
            UnsupportedCharsetException ex = captureUce(() -> Charset.forName(unknown),
                    "forName(" + unknown + ") -> UCE");
            check(unknown.equals(ex.getCharsetName()), "thrown UCE preserves name [" + unknown + "]");
        }
        // Illegal-syntax names are reported by ICE, not UCE.
        expect(IllegalCharsetNameException.class, () -> Charset.forName("bad name"),
                "forName(bad name) -> ICE, not UCE");
        expect(IllegalCharsetNameException.class, () -> Charset.forName(""),
                "forName(\"\") -> ICE, not UCE");
        // isSupported: unknown-but-legal name answers false; illegal name throws ICE.
        check(!Charset.isSupported("no-such-charset-xyz"), "isSupported(unknown) false, no throw");
        expect(IllegalCharsetNameException.class, () -> Charset.isSupported("bad name"),
                "isSupported(bad name) -> ICE");
    }

    // Serialization: fixed serialVersionUID + round trip preserving the requested name.
    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(UnsupportedCharsetException.class).getSerialVersionUID();
        check(suid == 1490765524727386367L, "serialVersionUID == 1490765524727386367L");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new UnsupportedCharsetException("no-such-charset"));
        }
        UnsupportedCharsetException rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (UnsupportedCharsetException) ois.readObject();
        }
        check(rt != null, "round-trip yields UCE");
        check("no-such-charset".equals(rt.getCharsetName()), "charset name survives round trip");
        check("no-such-charset".equals(rt.getMessage()), "message survives round trip");
        check(rt instanceof IllegalArgumentException, "hierarchy preserved after round trip");
        check(rt instanceof Serializable, "still Serializable after round trip");

        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new UnsupportedCharsetException(null));
        }
        UnsupportedCharsetException rtNull;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rtNull = (UnsupportedCharsetException) ois.readObject();
        }
        check(rtNull.getCharsetName() == null, "null charset name survives round trip");
    }

    // User subclasses may carry the exception through their own layers.
    static final class DetailedUce extends UnsupportedCharsetException {
        DetailedUce(String name) { super(name); }
    }

    static void testSubclass() {
        DetailedUce d = new DetailedUce("still-unknown");
        check("still-unknown".equals(d.getCharsetName()), "subclass ctor delegates name");
        check("still-unknown".equals(d.getMessage()), "subclass inherits message semantics");
        check(d instanceof UnsupportedCharsetException, "subclass instanceof UCE");
        expect(UnsupportedCharsetException.class, () -> {
            throw d;
        }, "subclass throwable as UCE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxSurface();
        testCtorMatrix();
        testForNameTriggers();
        testSerialization();
        testSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
