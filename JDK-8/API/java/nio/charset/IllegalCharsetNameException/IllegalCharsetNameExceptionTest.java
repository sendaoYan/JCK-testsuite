/*
 * @test
 * @summary java.nio.charset.IllegalCharsetNameException conformance: name accessor and forName.
 * @run main IllegalCharsetNameExceptionTest
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

public class IllegalCharsetNameExceptionTest {
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

    /** Runs r, expects IllegalCharsetNameException, returns it (never null on success). */
    static IllegalCharsetNameException captureIce(Runnable r, String msg) {
        try {
            r.run();
        } catch (IllegalCharsetNameException t) {
            passed++;
            return t;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testIllegalCharsetNameException() {
        IllegalCharsetNameException e = new IllegalCharsetNameException("bad name!");
        check("bad name!".equals(e.getCharsetName()), "getCharsetName");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
        try {
            Charset.forName("bad name!");
            check(false, "forName should throw");
        } catch (IllegalCharsetNameException expected) {
            check("bad name!".equals(expected.getCharsetName()), "forName throws");
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.IllegalCharsetNameException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalArgumentException.class, "superclass is IllegalArgumentException");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(Serializable.class.isAssignableFrom(c), "Serializable via RuntimeException");
        // sole accessor beyond Throwable state
        check(c.getDeclaredMethods().length == 1
                && "getCharsetName".equals(c.getDeclaredMethods()[0].getName())
                && c.getDeclaredMethods()[0].getReturnType() == String.class,
                "single declared method getCharsetName() returning String");
    }

    // Constructor matrix: message mirrors the offending name verbatim; a null name is
    // stored as null but rendered as the literal string "null" by String.valueOf.
    static void testCtorMatrix() {
        IllegalCharsetNameException e = new IllegalCharsetNameException("bad name!");
        check("bad name!".equals(e.getCharsetName()), "getCharsetName round trip");
        check("bad name!".equals(e.getMessage()), "getMessage mirrors charset name");
        check("java.nio.charset.IllegalCharsetNameException: bad name!".equals(e.toString()),
                "toString == className: name");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        IllegalCharsetNameException nullName = new IllegalCharsetNameException(null);
        check(nullName.getCharsetName() == null, "null name: getCharsetName() returns null, no throw");
        check("null".equals(nullName.getMessage()), "null name: getMessage() is literal \"null\"");

        IllegalCharsetNameException emptyName = new IllegalCharsetNameException("");
        check("".equals(emptyName.getCharsetName()), "empty name preserved verbatim");
        check("".equals(emptyName.getMessage()), "empty name message empty");

        check(e instanceof RuntimeException, "is RuntimeException");
        check(e instanceof IllegalArgumentException, "is IllegalArgumentException");
    }

    // Real triggers via Charset.forName/isSupported: name-syntax failures yield ICE with
    // the offending name preserved; syntactically legal but unknown names yield
    // UnsupportedCharsetException instead; null yields plain IllegalArgumentException.
    static void testForNameTriggers() {
        expect(IllegalArgumentException.class, () -> Charset.forName(null),
                "forName(null) -> IllegalArgumentException (not NPE/ICE)");
        check(!(new IllegalArgumentException("x") instanceof IllegalCharsetNameException),
                "ICE is more specific than the plain IAE thrown for null");

        IllegalCharsetNameException empty = captureIce(() -> Charset.forName(""), "forName(\"\")");
        check("".equals(empty.getCharsetName()), "empty name preserved by thrown exception");

        for (String bad : new String[]{
                "bad name", "bad\u0000name", "-ABC", ".ABC", ":ABC", "_ABC",
                "A#B", "A/B", "A?B", "UTF-8/x", new String(new char[]{'A', (char) 0x80, 'B'})}) {
            IllegalCharsetNameException ex = captureIce(() -> Charset.forName(bad),
                    "forName(" + bad + ") illegal");
            check(bad.equals(ex.getCharsetName()), "forName-thrown ICE preserves name [" + bad + "]");
        }

        // Digits are legal anywhere in a charset name (including the first character) and
        // so is '+'; those names are merely unsupported, not illegal.
        for (String legal : new String[]{"1ABC", "A+B", "no-such-charset-xyz"}) {
            expect(UnsupportedCharsetException.class, () -> Charset.forName(legal),
                    "forName(" + legal + ") legal-but-unknown -> UnsupportedCharsetException");
        }

        // isSupported shares the same name checker.
        expect(IllegalArgumentException.class, () -> Charset.isSupported(null),
                "isSupported(null) -> IllegalArgumentException");
        expect(IllegalCharsetNameException.class, () -> Charset.isSupported(""),
                "isSupported(\"\") -> IllegalCharsetNameException (not false)");
        expect(IllegalCharsetNameException.class, () -> Charset.isSupported("bad name"),
                "isSupported(bad name) -> IllegalCharsetNameException (not false)");
        check(!Charset.isSupported("no-such-charset-xyz"),
                "legal-but-unknown name: isSupported returns false without throwing");
    }

    // Serialization: fixed serialVersionUID + round trip preserving the offending name.
    static void testSerialization() throws Exception {
        long suid = ObjectStreamClass.lookup(IllegalCharsetNameException.class).getSerialVersionUID();
        check(suid == 1457525358470002989L, "serialVersionUID == 1457525358470002989L");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new IllegalCharsetNameException("bad name!"));
        }
        IllegalCharsetNameException rt;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rt = (IllegalCharsetNameException) ois.readObject();
        }
        check(rt != null, "round-trip yields ICE");
        check("bad name!".equals(rt.getCharsetName()), "charset name survives round trip");
        check("bad name!".equals(rt.getMessage()), "message survives round trip");
        check(rt instanceof IllegalArgumentException, "hierarchy preserved after round trip");
        check(rt instanceof Serializable, "still Serializable after round trip");

        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new IllegalCharsetNameException(null));
        }
        IllegalCharsetNameException rtNull;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            rtNull = (IllegalCharsetNameException) ois.readObject();
        }
        check(rtNull.getCharsetName() == null, "null charset name survives round trip");
    }

    // User subclasses may carry the exception through their own layers.
    static final class DetailedIce extends IllegalCharsetNameException {
        DetailedIce(String name) { super(name); }
    }

    static void testSubclass() {
        DetailedIce d = new DetailedIce("also bad?");
        check("also bad?".equals(d.getCharsetName()), "subclass ctor delegates name");
        check("also bad?".equals(d.getMessage()), "subclass inherits message semantics");
        check(d instanceof IllegalCharsetNameException, "subclass instanceof ICE");
        expect(IllegalCharsetNameException.class, () -> {
            throw d;
        }, "subclass throwable as ICE");
    }

    public static void main(String[] args) throws Exception {
        testIllegalCharsetNameException();
        testWhiteBox();
        testCtorMatrix();
        testForNameTriggers();
        testSerialization();
        testSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
