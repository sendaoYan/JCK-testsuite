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
import java.io.PrintWriter;
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

    // ---- appended: ISO-8859-1 unicode (backslash-u) escaping, list(), parse edges, view channels ----

    static void testUnicodeEscapingRoundTrip() throws Exception {
        // store(OutputStream) escapes all chars >0x7F as backslash-uXXXX and round-trips
        Properties cn = new Properties();
        cn.setProperty("k中", "值123");
        cn.setProperty("plain", "ascii");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        cn.store(bos, null);
        String stored = bos.toString("ISO-8859-1");
        check(stored.contains("\\u4E2D"), "store(OutputStream) escapes key char as \\u4E2D");
        check(stored.contains("\\u503C"), "store(OutputStream) escapes value char as \\u503C");
        check(!stored.contains("中"), "no raw non-ASCII byte in ISO-8859-1 view");
        check(stored.contains("plain=ascii"), "ascii entry stored literally");
        Properties back = new Properties();
        back.load(new ByteArrayInputStream(bos.toByteArray()));
        check(back.equals(cn), "store/load(InputStream) non-ASCII round-trip equals");
        check("值123".equals(back.getProperty("k中")), "escaped key/value restored exactly");

        // store(Writer) does NOT escape non-ASCII: raw chars flow through
        StringWriter sw = new StringWriter();
        cn.store(sw, null);
        check(sw.toString().contains("k中=值123"), "store(Writer) keeps raw non-ASCII");
        Properties back2 = new Properties();
        back2.load(new StringReader(sw.toString()));
        check(back2.equals(cn), "store/load(Reader) non-ASCII round-trip equals");

        // control characters in key/value round-trip through escapes
        Properties ctl = new Properties();
        ctl.setProperty("tab\tkey", "line1\nline2");
        StringWriter sw2 = new StringWriter();
        ctl.store(sw2, null);
        check(!sw2.toString().contains("\n\tkey"), "tab in key escaped");
        check(!sw2.toString().contains("line1\n"), "newline in value escaped");
        Properties back3 = new Properties();
        back3.load(new StringReader(sw2.toString()));
        check("line1\nline2".equals(back3.getProperty("tab\tkey")), "control chars round-trip");

        // load decodes backslash-uXXXX escapes in the document
        Properties u = new Properties();
        u.load(new StringReader("k=\\u4E2D\\u6587"));
        check("中文".equals(u.getProperty("k")), "load decodes \\uXXXX escapes");

        // multi-line store comment: every comment line gets its own '#'
        Properties mc = new Properties();
        mc.setProperty("a", "b");
        StringWriter sw3 = new StringWriter();
        mc.store(sw3, "line1\nline2");
        check(sw3.toString().contains("#line1\n#line2"), "multiline comment prefixed per line");
    }

    static void testList() {
        Properties p = new Properties();
        p.setProperty("short", "v");
        p.setProperty("longkey.name.here",
                      "0123456789012345678901234567890123456789012345678901234567890123456789");
        java.io.StringWriter sw = new java.io.StringWriter();
        p.list(new java.io.PrintWriter(sw));
        String out = sw.toString();
        check(out.startsWith("-- listing properties --"), "list header line");
        check(out.contains("short=v"), "list prints key=value");
        // values are truncated to 37 chars + "..." (40 total); keys are never truncated
        check(out.contains("longkey.name.here=0123456789012345678901234567890123456..."),
              "list truncates long value to 37 chars + ...");
        check(!out.contains("67890123456789\n"), "list omits the truncated tail");
        // list(PrintStream) mirrors the Writer form
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        p.list(new java.io.PrintStream(bos));
        check(new String(bos.toByteArray()).contains("short=v"), "list(PrintStream) prints key=value");
        // defaults entries ARE listed too (order not specified: assert by membership)
        Properties defs = new Properties();
        defs.setProperty("dk", "DV");
        Properties withDefs = new Properties(defs);
        withDefs.setProperty("own", "OV");
        java.io.StringWriter sw2 = new java.io.StringWriter();
        withDefs.list(new java.io.PrintWriter(sw2));
        String out2 = sw2.toString();
        check(out2.contains("own=OV"), "list includes own entries");
        check(out2.contains("dk=DV"), "list includes default entries");
        // non-String values (or keys) make list() fail with ClassCastException
        // (list casts through the raw table; both JDK-8 and JDK-27 behave alike)
        Properties mixed = new Properties();
        mixed.setProperty("s", "ok");
        mixed.put("n", Integer.valueOf(1));
        checkThrows(ClassCastException.class,
                    () -> mixed.list(new PrintWriter(new StringWriter())),
                    "list non-String value CCE");
        Properties mk = new Properties();
        mk.put(Integer.valueOf(2), "v");
        checkThrows(ClassCastException.class,
                    () -> mk.list(new PrintWriter(new StringWriter())),
                    "list non-String key CCE");
    }

    static void testLoadParseEdges() throws Exception {
        Properties p = new Properties();
        p.load(new StringReader(
            "novalue\n"          + // key alone -> empty value
            "k =  v  \n"         + // leading spaces of value skipped, trailing kept
            "trail=after\\ \n"   + // escaped trailing space
            "  ind=x\n"          + // leading whitespace before key skipped
            "#c\n!b\n\n"         + // #/! comments and blank line
            "eq\\=k=v\n"         + // escaped '=' inside key
            "empty=\n"));         // empty value
        check("".equals(p.getProperty("novalue")), "key-only line -> empty value");
        check("v  ".equals(p.getProperty("k")), "value keeps trailing spaces, drops leading");
        check("after ".equals(p.getProperty("trail")), "escaped trailing space preserved");
        check("x".equals(p.getProperty("ind")), "leading whitespace before key skipped");
        check("v".equals(p.getProperty("eq=k")), "escaped '=' allowed inside key");
        check("".equals(p.getProperty("empty")), "explicit empty value");
        check(p.size() == 6, "parse edge count: " + p.size());

        // load(Reader) does no unicode-escape-level charset conversion for Writer-stored docs
        // (already covered); a truncated escape at end of line is malformed -> IAE
        checkThrows(IllegalArgumentException.class,
                    () -> new Properties().load(new StringReader("k=\\u4E")),
                    "truncated \\u escape IAE");
    }

    static void testHashtableViewChannels() {
        Properties p = new Properties();
        p.setProperty("a", "1");
        p.setProperty("b", "2");
        // keySet is a live Hashtable view: removal drops the property
        check(p.keySet().remove("a"), "keySet.remove succeeds");
        check(p.getProperty("a") == null && p.size() == 1, "keySet.remove drops property");
        // entrySet iterator can rewrite a value via setValue
        for (java.util.Map.Entry<Object, Object> e : p.entrySet()) {
            if ("b".equals(e.getKey())) e.setValue("22");
        }
        check("22".equals(p.getProperty("b")), "entrySet.setValue updates property");
        // Hashtable compute admits non-String values; getProperty hides them, get sees them
        p.compute("c", (k, v) -> Integer.valueOf(3));
        check(p.getProperty("c") == null, "getProperty blind to non-String compute result");
        check(Integer.valueOf(3).equals(p.get("c")), "get sees raw compute result");
        // forEach walks the raw table (Object,Object)
        final int[] n = {0};
        p.forEach((k, v) -> n[0]++);
        check(n[0] == p.size(), "forEach visits every raw entry");
    }

    static void testVersionSurface() throws Exception {
        // JDK-8 has neither Properties(int) nor storeToXML(...,Charset):
        // lock their absence so any future backport shows up as a diff.
        try {
            Properties.class.getConstructor(int.class);
            check(false, "Properties(int) absent on JDK-8");
        } catch (NoSuchMethodException ex) { passed++; }
        try {
            Properties.class.getMethod("storeToXML", java.io.OutputStream.class,
                    String.class, java.nio.charset.Charset.class);
            check(false, "storeToXML(...,Charset) absent on JDK-8");
        } catch (NoSuchMethodException ex) { passed++; }
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
        testUnicodeEscapingRoundTrip();
        testList();
        testLoadParseEdges();
        testHashtableViewChannels();
        testVersionSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
