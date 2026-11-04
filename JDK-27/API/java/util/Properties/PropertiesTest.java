/*
 * @test
 * @summary java.util.Properties conformance: white-box structure plus black-box behavior of
 *          setProperty/getProperty with defaults, size/containsKey, stringPropertyNames,
 *          propertyNames, store/load round-trip via StringWriter/StringReader, and put/get
 *          (non-string values).
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

    static Properties roundTrip(Properties p) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(p); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Properties) ois.readObject();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Properties");
        check(!c.isInterface(), "Properties is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Properties is concrete");
        check(java.util.Hashtable.class.isAssignableFrom(c), "Properties extends Hashtable");
        c.getMethod("setProperty", String.class, String.class);
        c.getMethod("getProperty", String.class);
        c.getMethod("getProperty", String.class, String.class);
        c.getMethod("stringPropertyNames");
        c.getMethod("propertyNames");
        c.getMethod("load", java.io.Reader.class);
        c.getMethod("store", java.io.Writer.class, String.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testBasics();
        testDefaults();
        testPropertyNames();
        testStoreLoadRoundTrip();
        testNonStringValues();
    }

    static void testBasics() {
        Properties p = new Properties();
        check(p.setProperty("k", "v") == null, "setProperty returns previous (null)");
        check("v".equals(p.getProperty("k")), "setProperty/getProperty");
        check("default".equals(p.getProperty("missing", "default")), "getProperty with default");
        check(p.size() == 1 && p.containsKey("k"), "size/containsKey");
        check(p.setProperty("k", "v2").equals("v"), "setProperty returns previous value");
        check("v2".equals(p.getProperty("k")), "overwrite updates value");
    }

    static void testDefaults() {
        Properties defs = new Properties();
        defs.setProperty("x", "X");
        Properties p = new Properties(defs);
        check("X".equals(p.getProperty("x")), "defaults via constructor");
        p.setProperty("x", "Y");
        check("Y".equals(p.getProperty("x")), "own value overrides default");
        p.remove("x");
        check("X".equals(p.getProperty("x")), "remove falls back to default");
    }

    static void testPropertyNames() {
        Properties defs = new Properties();
        defs.setProperty("d", "D");
        Properties p = new Properties(defs);
        p.setProperty("k", "v");
        Set<String> names = p.stringPropertyNames();
        check(names.contains("k") && names.contains("d"),
              "stringPropertyNames includes own and default string keys");
        boolean hasD = false, hasK = false;
        Enumeration<?> en = p.propertyNames();
        while (en.hasMoreElements()) {
            String n = (String) en.nextElement();
            if ("d".equals(n)) hasD = true;
            if ("k".equals(n)) hasK = true;
        }
        check(hasD && hasK, "propertyNames includes own + default keys");
    }

    static void testStoreLoadRoundTrip() {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "two words");
        p.setProperty("c", "with=equals");
        StringWriter sw = new StringWriter();
        try {
            p.store(sw, "test comment");
        } catch (Exception e) {
            throw new AssertionError("store failed: " + e);
        }
        Properties q = new Properties();
        try {
            q.load(new StringReader(sw.toString()));
        } catch (Exception e) {
            throw new AssertionError("load failed: " + e);
        }
        check("1".equals(q.getProperty("a")), "store/load roundtrip a");
        check("two words".equals(q.getProperty("b")), "store/load roundtrip b");
        check("with=equals".equals(q.getProperty("c")), "store/load roundtrip c");
        check(q.size() == 3, "store/load roundtrip size");
    }

    static void testNonStringValues() {
        Properties p = new Properties();
        p.put("n", Integer.valueOf(5));
        check(Integer.valueOf(5).equals(p.get("n")), "put/get returns Object value");
        check(p.getProperty("n") == null, "getProperty ignores non-String value");
    }

    // ---- Part 3: deepened contract coverage ---------------------------------

    static void testConstructorMatrix() {
        Properties p = new Properties();
        check(p.isEmpty(), "default ctor empty");
        Properties defaults = new Properties();
        defaults.setProperty("d", "D");
        Properties withDefaults = new Properties(defaults);
        check("D".equals(withDefaults.getProperty("d")), "ctor(Properties) links defaults");
        withDefaults.setProperty("d", "own");
        check("own".equals(withDefaults.getProperty("d")), "own overrides default");
        withDefaults.remove("d");
        check("D".equals(withDefaults.getProperty("d")), "remove falls back to default");
        defaults.setProperty("late", "L");
        check("L".equals(withDefaults.getProperty("late")), "defaults are live (not copied)");
        Properties nullDefaults = new Properties((Properties) null);
        check(nullDefaults.isEmpty() && nullDefaults.getProperty("x") == null,
              "Properties(null defaults) behaves like empty");
    }

    static void testSetGetPropertyContract() {
        Properties p = new Properties();
        check(p.setProperty("k", "v") == null, "setProperty returns null for new key");
        check(p.setProperty("k", "v2").equals("v"), "setProperty returns previous value");
        check("v2".equals(p.getProperty("k")), "getProperty returns current value");
        check(p.getProperty("missing") == null, "getProperty missing returns null");
        check("fallback".equals(p.getProperty("missing", "fallback")), "getProperty(key, default) default");
        check("v2".equals(p.getProperty("k", "fallback")), "getProperty(key, default) value when present");
        p.put("n", Integer.valueOf(7));
        check(p.getProperty("n") == null, "getProperty ignores non-String value");
        check("def".equals(p.getProperty("n", "def")), "getProperty(key, default) default for non-String value");
        checkThrows(NullPointerException.class, () -> p.setProperty(null, "v"), "setProperty(null key) NPE");
        checkThrows(NullPointerException.class, () -> p.setProperty("k", null), "setProperty(null value) NPE");
        checkThrows(NullPointerException.class, () -> p.getProperty(null), "getProperty(null) NPE");
        checkThrows(NullPointerException.class, () -> p.getProperty(null, "d"), "getProperty(null, default) NPE");
        checkThrows(NullPointerException.class, () -> p.put("nullv", null), "put(null value) NPE");
        checkThrows(NullPointerException.class, () -> p.put(null, "v"), "put(null key) NPE");
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

    static void testPropertyNamesContract() {
        Properties defaults = new Properties();
        defaults.setProperty("d", "D");
        Properties clean = new Properties(defaults);
        clean.setProperty("k", "v");
        Set<String> names = clean.stringPropertyNames();
        check(names.contains("k") && names.contains("d"), "stringPropertyNames own + default");
        // JDK-27 returns an unmodifiable set (unlike JDK-8's Hashtable keySet).
        checkThrows(UnsupportedOperationException.class, () -> names.remove("k"),
                    "stringPropertyNames returns unmodifiable set");
        check(clean.containsKey("k"), "Properties still holds k");
        clean.setProperty("after", "x");
        check(!names.contains("after"), "stringPropertyNames snapshot not live");

        Properties mixed = new Properties();
        mixed.setProperty("k", "v");
        mixed.put(Integer.valueOf(99), "vs");
        mixed.put("sv", Integer.valueOf(5));
        Set<String> mixedNames = mixed.stringPropertyNames();
        check(mixedNames.contains("k"), "stringPropertyNames keeps string entry");
        check(!mixedNames.contains(Integer.valueOf(99)) && !mixedNames.contains("sv"),
              "stringPropertyNames omits non-String key or value");

        Enumeration<?> en = clean.propertyNames();
        boolean hasK = false, hasD = false;
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            if ("k".equals(key)) hasK = true;
            if ("d".equals(key)) hasD = true;
        }
        check(hasK && hasD, "propertyNames own + default");
        checkThrows(ClassCastException.class, () -> mixed.propertyNames().hasMoreElements(),
                    "propertyNames with non-String key CCE");
    }

    static void testStoreLoadContract() throws Exception {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "two words");
        p.setProperty("c", "with=equals");
        p.setProperty("d", "colon:value");
        p.setProperty("e", "hash#and!bang");
        p.setProperty("f", " leading space");
        StringWriter sw = new StringWriter();
        p.store(sw, null);
        Properties q = new Properties();
        q.load(new StringReader(sw.toString()));
        check(q.equals(p), "store(Writer,null)/load(Reader) round-trip equals");
        check("with=equals".equals(q.getProperty("c")), "round-trip escaped =");
        check("colon:value".equals(q.getProperty("d")), "round-trip escaped :");
        check("hash#and!bang".equals(q.getProperty("e")), "round-trip escaped # and !");
        check(" leading space".equals(q.getProperty("f")), "round-trip leading space");

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

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.store(bos, "c");
        Properties q2 = new Properties();
        q2.load(new ByteArrayInputStream(bos.toByteArray()));
        check(q2.equals(p), "store(OutputStream)/load(InputStream) round-trip");
    }

    static void testLoadEdgeCases() throws Exception {
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
        check("value".equals(p.getProperty("spaced key")), "load escaped space in key");

        checkThrows(IllegalArgumentException.class,
                    () -> new Properties().load(new StringReader("k=\\uZZZZ")),
                    "load malformed unicode escape IAE");
        checkThrows(NullPointerException.class,
                    () -> new Properties().load((java.io.Reader) null), "load(null Reader) NPE");
        checkThrows(NullPointerException.class,
                    () -> new Properties().load((java.io.InputStream) null), "load(null InputStream) NPE");
    }

    static void testStoreErrors() {
        checkThrows(NullPointerException.class,
                    () -> new Properties().store((java.io.Writer) null, "c"), "store(null Writer) NPE");
        checkThrows(NullPointerException.class,
                    () -> new Properties().store((java.io.OutputStream) null, "c"), "store(null OutputStream) NPE");
        final Properties pv = new Properties();
        pv.put("k", Integer.valueOf(5));
        checkThrows(ClassCastException.class,
                    () -> pv.store(new StringWriter(), "c"), "store non-String value CCE");
        final Properties pk = new Properties();
        pk.put(Integer.valueOf(1), "v");
        checkThrows(ClassCastException.class,
                    () -> pk.store(new StringWriter(), "c"), "store non-String key CCE");
    }

    static void testStoreToXMLContract() throws Exception {
        Properties p = new Properties();
        p.setProperty("k", "v");
        p.setProperty("with space", "value with space");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        p.storeToXML(bos, "comment");
        Properties q = new Properties();
        q.loadFromXML(new ByteArrayInputStream(bos.toByteArray()));
        check(q.equals(p), "storeToXML/loadFromXML round-trip");

        checkThrows(NullPointerException.class,
                    () -> p.storeToXML((java.io.OutputStream) null, "c"), "storeToXML(null) NPE");
        checkThrows(NullPointerException.class,
                    () -> p.loadFromXML(null), "loadFromXML(null) NPE");
        checkThrows(InvalidPropertiesFormatException.class,
                    () -> new Properties().loadFromXML(
                        new ByteArrayInputStream("<not-properties/>".getBytes("UTF-8"))),
                    "loadFromXML malformed -> InvalidPropertiesFormatException");
        checkThrows(UnsupportedEncodingException.class,
                    () -> p.storeToXML(new ByteArrayOutputStream(), "c", "no-such-encoding-xyz"),
                    "storeToXML bad encoding -> UnsupportedEncodingException");

        // JDK-27 storeToXML throws ClassCastException for non-String key/value.
        final Properties pv = new Properties();
        pv.put("k", Integer.valueOf(5));
        checkThrows(ClassCastException.class,
                    () -> pv.storeToXML(new ByteArrayOutputStream(), "c"), "JDK-27 storeToXML non-String CCE");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorMatrix();
        testSetGetPropertyContract();
        testDefaultsChain();
        testPropertyNamesContract();
        testStoreLoadContract();
        testLoadEdgeCases();
        testStoreErrors();
        testStoreToXMLContract();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
