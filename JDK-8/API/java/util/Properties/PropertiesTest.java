/*
 * @test
 * @summary java.util.Properties API conformance: full contract coverage.
 *          constructor matrix (empty, defaults-linked, null defaults NPE),
 *          setProperty/getProperty String-only semantics + exact null NPEs,
 *          defaults fallback/override/remove/chain, propertyNames vs
 *          stringPropertyNames (non-backed copy, non-String omission, CCE),
 *          store/load round-trip (escaping, comments, line continuation),
 *          load malformed unicode escape IAE + null reader NPE, store NPE/CCE,
 *          storeToXML/loadFromXML round-trip + NPE + InvalidPropertiesFormatException
 *          + UnsupportedEncodingException, serialization round-trip preserving
 *          defaults + serialVersionUID.
 * @run main PropertiesTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

public class PropertiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static Properties roundTrip(Properties p) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(p); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Properties) ois.readObject();
        }
    }

    static void testConstructorMatrix() {
        Properties p = new Properties();
        check(p.isEmpty(), "default ctor empty");

        Properties defaults = new Properties();
        defaults.setProperty("d", "D");
        Properties withDefaults = new Properties(defaults);
        check("D".equals(withDefaults.getProperty("d")), "ctor(Properties) links defaults");
        withDefaults.setProperty("d", "own");
        check("own".equals(withDefaults.getProperty("d")), "own value overrides default");
        withDefaults.remove("d");
        check("D".equals(withDefaults.getProperty("d")), "remove falls back to default");
        defaults.setProperty("late", "L");
        check("L".equals(withDefaults.getProperty("late")), "defaults are live (not copied)");

        // Properties(null defaults) is allowed: null defaults is equivalent to no defaults.
        Properties nullDefaults = new Properties((Properties) null);
        check(nullDefaults.isEmpty() && nullDefaults.getProperty("x") == null,
              "Properties(null defaults) behaves like empty (no NPE)");
    }

    static void testSetGetProperty() {
        Properties p = new Properties();
        check(p.setProperty("k", "v") == null, "setProperty returns null for new key");
        check(p.setProperty("k", "v2").equals("v"), "setProperty returns previous value");
        check("v2".equals(p.getProperty("k")), "getProperty returns current value");
        check(p.getProperty("missing") == null, "getProperty missing returns null");
        check("fallback".equals(p.getProperty("missing", "fallback")),
              "getProperty(key, default) returns default");
        check("v2".equals(p.getProperty("k", "fallback")),
              "getProperty(key, default) returns value when present");

        // String-only semantics: non-String values are invisible to getProperty
        p.put("n", Integer.valueOf(7));
        check(p.getProperty("n") == null, "getProperty ignores non-String value");
        check("def".equals(p.getProperty("n", "def")), "getProperty(key, default) default for non-String value");
        // Hashtable inheritance rejects null values/keys via put
        checkThrows(NullPointerException.class, () -> p.put("nullv", null), "put(null value) NPE");
        checkThrows(NullPointerException.class, () -> p.put(null, "v"), "put(null key) NPE");

        // exact null rejections
        checkThrows(NullPointerException.class, () -> p.setProperty(null, "v"), "setProperty(null key) NPE");
        checkThrows(NullPointerException.class, () -> p.setProperty("k", null), "setProperty(null value) NPE");
        checkThrows(NullPointerException.class, () -> p.getProperty(null), "getProperty(null) NPE");
        checkThrows(NullPointerException.class, () -> p.getProperty(null, "d"), "getProperty(null, default) NPE");
    }

    static void testDefaultsChain() {
        Properties grand = new Properties();
        grand.setProperty("a", "A");
        Properties parent = new Properties(grand);
        parent.setProperty("b", "B");
        Properties child = new Properties(parent);
        child.setProperty("c", "C");
        check("A".equals(child.getProperty("a")), "multi-level defaults fallback");
        check("B".equals(child.getProperty("b")), "mid-level defaults fallback");
        check("C".equals(child.getProperty("c")), "own value");
        child.setProperty("a", "childA");
        check("childA".equals(child.getProperty("a")), "own value shadows grand default");
    }

    static void testPropertyNames() {
        Properties defaults = new Properties();
        defaults.setProperty("d", "D");
        Properties clean = new Properties(defaults);
        clean.setProperty("k", "v");

        // stringPropertyNames includes own + default String key/value entries
        Set<String> names = clean.stringPropertyNames();
        check(names.contains("k") && names.contains("d"), "stringPropertyNames includes own + default string entries");

        // stringPropertyNames is NOT backed by the Properties object:
        // removing from the returned set does not affect the Properties.
        check(names.remove("k"), "returned set supports remove");
        check(clean.containsKey("k"), "remove on returned set does not affect Properties");
        clean.setProperty("after", "x");
        check(!names.contains("after"), "stringPropertyNames snapshot not live");

        // non-String key or value entries are omitted by stringPropertyNames
        Properties mixed = new Properties();
        mixed.setProperty("k", "v");
        mixed.put(Integer.valueOf(99), "vs");  // key not a String
        mixed.put("sv", Integer.valueOf(5));   // value not a String
        Set<String> mixedNames = mixed.stringPropertyNames();
        check(mixedNames.contains("k"), "stringPropertyNames keeps string entry");
        check(!mixedNames.contains(Integer.valueOf(99)) && !mixedNames.contains("sv"),
              "stringPropertyNames omits non-String key or value");

        // propertyNames enumerates own + default keys (all String keys here)
        Enumeration<?> en = clean.propertyNames();
        boolean hasK = false, hasD = false;
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            if ("k".equals(key)) hasK = true;
            if ("d".equals(key)) hasD = true;
        }
        check(hasK && hasD, "propertyNames includes own + default keys");

        // propertyNames throws CCE when a key is non-String
        checkThrows(ClassCastException.class, () -> mixed.propertyNames().hasMoreElements(),
                    "propertyNames with non-String key CCE");
    }

    static void testStoreLoadRoundTrip() throws Exception {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "two words");
        p.setProperty("c", "with=equals");
        p.setProperty("d", "colon:value");
        p.setProperty("e", "hash#and!bang");
        p.setProperty("f", " leading space");

        StringWriter sw = new StringWriter();
        p.store(sw, null); // null comments allowed
        Properties q = new Properties();
        q.load(new StringReader(sw.toString()));
        check(q.equals(p), "store(Writer,null)/load(Reader) round-trip equals");
        check("1".equals(q.getProperty("a")), "round-trip a");
        check("two words".equals(q.getProperty("b")), "round-trip b");
        check("with=equals".equals(q.getProperty("c")), "round-trip escaped =");
        check("colon:value".equals(q.getProperty("d")), "round-trip escaped :");
        check("hash#and!bang".equals(q.getProperty("e")), "round-trip escaped # and !");
        check(" leading space".equals(q.getProperty("f")), "round-trip leading space");

        // defaults are NOT written by store
        Properties defs = new Properties();
        defs.setProperty("dk", "DV");
        Properties withDefs = new Properties(defs);
        withDefs.setProperty("own", "OV");
        StringWriter sw2 = new StringWriter();
        withDefs.store(sw2, "comment");
        Properties loaded = new Properties();
        loaded.load(new StringReader(sw2.toString()));
        check("OV".equals(loaded.getProperty("own")), "store writes own entries");
        check(loaded.getProperty("dk") == null, "store does NOT write defaults");

        // store(OutputStream, comments)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.store(bos, "c");
        Properties q2 = new Properties();
        q2.load(new ByteArrayInputStream(bos.toByteArray()));
        check(q2.equals(p), "store(OutputStream)/load(InputStream) round-trip");
    }

    static void testLoadEdgeCases() throws Exception {
        // comments (# and !), blank lines, line continuation, escaped separators
        String input =
            "# comment line\n" +
            "! bang comment\n" +
            "\n" +
            "key1=value1\n" +
            "key2:value2\n" +
            "continued=line\\\n" +
            "cont\n" +
            "spaced\\ key=value\n";
        Properties p = new Properties();
        p.load(new StringReader(input));
        check("value1".equals(p.getProperty("key1")), "load '=' separator");
        check("value2".equals(p.getProperty("key2")), "load ':' separator");
        check("linecont".equals(p.getProperty("continued")), "load line continuation");
        check("value".equals(p.getProperty("spaced key")), "load key with space before '='");

        // malformed unicode escape -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class,
                    () -> new Properties().load(new StringReader("k=\\uZZZZ")),
                    "load malformed unicode escape IAE");
        // null reader/stream -> NPE
        checkThrows(NullPointerException.class,
                    () -> new Properties().load((java.io.Reader) null), "load(null Reader) NPE");
        checkThrows(NullPointerException.class,
                    () -> new Properties().load((java.io.InputStream) null), "load(null InputStream) NPE");
    }

    static void testStoreErrors() {
        // null writer/stream -> NPE
        checkThrows(NullPointerException.class,
                    () -> new Properties().store((java.io.Writer) null, "c"), "store(null Writer) NPE");
        checkThrows(NullPointerException.class,
                    () -> new Properties().store((java.io.OutputStream) null, "c"), "store(null OutputStream) NPE");

        // non-String value -> ClassCastException
        final Properties pv = new Properties();
        pv.put("k", Integer.valueOf(5));
        checkThrows(ClassCastException.class,
                    () -> pv.store(new StringWriter(), "c"), "store non-String value CCE");

        // non-String key -> ClassCastException
        final Properties pk = new Properties();
        pk.put(Integer.valueOf(1), "v");
        checkThrows(ClassCastException.class,
                    () -> pk.store(new StringWriter(), "c"), "store non-String key CCE");
    }

    static void testStoreToXMLLoadFromXML() throws Exception {
        Properties p = new Properties();
        p.setProperty("k", "v");
        p.setProperty("with space", "value with space");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.storeToXML(bos, "comment");
        Properties q = new Properties();
        q.loadFromXML(new ByteArrayInputStream(bos.toByteArray()));
        check(q.equals(p), "storeToXML/loadFromXML round-trip");

        // null os -> NPE
        checkThrows(NullPointerException.class,
                    () -> p.storeToXML((java.io.OutputStream) null, "c"), "storeToXML(null) NPE");
        // null in -> NPE
        checkThrows(NullPointerException.class,
                    () -> p.loadFromXML(null), "loadFromXML(null) NPE");
        // malformed XML -> InvalidPropertiesFormatException
        checkThrows(InvalidPropertiesFormatException.class,
                    () -> new Properties().loadFromXML(
                        new ByteArrayInputStream("<not-properties/>".getBytes("UTF-8"))),
                    "loadFromXML malformed -> InvalidPropertiesFormatException");
        // bad encoding -> UnsupportedEncodingException
        checkThrows(UnsupportedEncodingException.class,
                    () -> p.storeToXML(new ByteArrayOutputStream(), "c", "no-such-encoding-xyz"),
                    "storeToXML bad encoding -> UnsupportedEncodingException");
        // JDK-8 storeToXML silently skips non-String key/value entries (no CCE).
        final Properties pv = new Properties();
        pv.put("k", Integer.valueOf(5));
        ByteArrayOutputStream skipBos = new ByteArrayOutputStream();
        pv.storeToXML(skipBos, "c");
        Properties skipped = new Properties();
        skipped.loadFromXML(new ByteArrayInputStream(skipBos.toByteArray()));
        check(skipped.isEmpty(), "JDK-8 storeToXML silently skips non-String entries");
    }

    static void testSerialization() throws Exception {
        Properties defaults = new Properties();
        defaults.setProperty("dk", "DV");
        Properties p = new Properties(defaults);
        p.setProperty("k", "v");

        Properties r = roundTrip(p);
        check(r != p, "round-trip distinct object");
        check("v".equals(r.getProperty("k")), "round-trip preserves own entries");
        check("DV".equals(r.getProperty("dk")), "round-trip preserves defaults");
        check(r.size() == 1, "round-trip size excludes defaults table entries");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Properties.class);
        check(osc.getSerialVersionUID() == 4112578634029874840L, "Properties serialVersionUID stable");
    }

    static void testStateInvariants() {
        Properties p = new Properties();
        check(p.isEmpty() && p.size() == 0, "empty initially");
        p.setProperty("a", "1");
        p.setProperty("b", "2");
        check(p.size() == 2, "size counts entries");
        p.setProperty("a", "10");
        check(p.size() == 2, "setProperty overwrite keeps size");
        p.clear();
        check(p.isEmpty(), "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testConstructorMatrix();
        testSetGetProperty();
        testDefaultsChain();
        testPropertyNames();
        testStoreLoadRoundTrip();
        testLoadEdgeCases();
        testStoreErrors();
        testStoreToXMLLoadFromXML();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
