/*
 * @test
 * @summary java.util.jar.Manifest conformance: white-box structure + black-box behavior.
 * @run main ManifestTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.Manifest");
        check(!c.isInterface() && java.lang.reflect.Modifier.isPublic(c.getModifiers()),
              "Manifest is public concrete class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasConstructor(c), "Manifest()");
        check(hasConstructor(c, InputStream.class), "Manifest(InputStream)");
        check(hasConstructor(c, Manifest.class), "Manifest(Manifest)");
        check(hasMethod(c, "getMainAttributes"), "getMainAttributes()");
        check(hasMethod(c, "getEntries"), "getEntries()");
        check(hasMethod(c, "getAttributes", String.class), "getAttributes(String)");
        check(hasMethod(c, "clear"), "clear()");
        check(hasMethod(c, "write", java.io.OutputStream.class), "write(OutputStream)");
        check(hasMethod(c, "read", InputStream.class), "read(InputStream)");
        check(hasMethod(c, "equals", Object.class), "equals(Object)");
        check(hasMethod(c, "hashCode"), "hashCode()");
        check(hasMethod(c, "clone"), "clone()");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testEmpty() {
        Manifest m = new Manifest();
        check(m.getMainAttributes().isEmpty(), "empty main attributes");
        check(m.getEntries().isEmpty(), "no entries");
        check(m.getAttributes("anything") == null, "getAttributes unknown null");
        check(m.getAttributes(null) == null, "getAttributes(null) null");
    }

    static void testMainAndEntries() {
        Manifest m = new Manifest();
        Attributes main = m.getMainAttributes();
        main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        main.put(Attributes.Name.MAIN_CLASS, "com.example.Main");
        check("1.0".equals(m.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "set/get main attribute");
        check("com.example.Main".equals(m.getMainAttributes().getValue("Main-Class")),
              "main attribute via string");

        Attributes sec = new Attributes();
        sec.put(Attributes.Name.SEALED, "true");
        sec.put(Attributes.Name.CONTENT_TYPE, "application/x");
        m.getEntries().put("com/example/", sec);
        check(m.getEntries().size() == 1, "one entry section");
        check(m.getAttributes("com/example/") == sec, "getAttributes returns stored section");
        check("true".equals(m.getAttributes("com/example/").getValue(Attributes.Name.SEALED)),
              "entry attribute value");
    }

    static void testWriteReadRoundTrip() throws Exception {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "My App");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.SEALED, "true");
        m.getEntries().put("pkg/", sec);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout);
        byte[] raw = bout.toByteArray();
        String text = new String(raw, StandardCharsets.UTF_8);
        check(text.contains("Manifest-Version: 1.0\r\n"), "write emits CRLF version line");
        check(text.contains("Name: pkg/\r\n"), "write emits Name section");

        Manifest m2 = new Manifest(new ByteArrayInputStream(raw));
        check("1.0".equals(m2.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "read back version");
        check("My App".equals(m2.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE)),
              "read back main attr");
        check("true".equals(m2.getAttributes("pkg/").getValue(Attributes.Name.SEALED)),
              "read back entry attr");
        check(m.equals(m2), "equals after round-trip");
        check(m.hashCode() == m2.hashCode(), "hashCode after round-trip");
    }

    static void testCopyAndClone() {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Attributes sec = new Attributes();
        sec.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        m.getEntries().put("a/", sec);

        Manifest copy = new Manifest(m);
        check(copy.equals(m) && copy.hashCode() == m.hashCode(), "copy equals original");
        copy.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "X");
        check(m.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS) == null,
              "copy main attributes independent");

        Manifest clone = (Manifest) m.clone();
        check(clone != m && clone.equals(m) && clone.hashCode() == m.hashCode(),
              "clone equals original and distinct");
        clone.clear();
        check(m.getEntries().size() == 1 && clone.getEntries().isEmpty(),
              "clone.clear independent of original");
    }

    static void testReadMerges() throws Exception {
        Manifest source = new Manifest();
        source.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Attributes a = new Attributes();
        a.put(Attributes.Name.SEALED, "true");
        source.getEntries().put("a/", a);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        source.write(bout);

        Manifest target = new Manifest();
        Attributes b = new Attributes();
        b.put(Attributes.Name.CONTENT_TYPE, "text/plain");
        target.getEntries().put("b/", b);

        target.read(new ByteArrayInputStream(bout.toByteArray()));
        check("1.0".equals(target.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "read merges main attributes");
        check(target.getEntries().size() == 2, "read merges entries (2 total)");
        check("true".equals(target.getAttributes("a/").getValue(Attributes.Name.SEALED)),
              "read-added entry present");
        check("text/plain".equals(target.getAttributes("b/").getValue(Attributes.Name.CONTENT_TYPE)),
              "pre-existing entry preserved");
    }

    static void testMalformedInput() {
        final byte[] bad = "badheader\n".getBytes(StandardCharsets.UTF_8);
        expectThrows(java.io.IOException.class, () -> new Manifest(new ByteArrayInputStream(bad)));
    }

    static void testClearAndToString() {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m.getEntries().put("x/", new Attributes());
        m.clear();
        check(m.getMainAttributes().isEmpty(), "clear main attributes");
        check(m.getEntries().isEmpty(), "clear entries");
        check(m.toString() != null, "toString non-null");
    }

    static void testBlackBox() throws Exception {
        testEmpty();
        testMainAndEntries();
        testWriteReadRoundTrip();
        testCopyAndClone();
        testReadMerges();
        testMalformedInput();
        testClearAndToString();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
