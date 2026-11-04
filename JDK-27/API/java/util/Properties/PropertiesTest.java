/*
 * @test
 * @summary java.util.Properties conformance: white-box structure plus black-box behavior of
 *          setProperty/getProperty with defaults, size/containsKey, stringPropertyNames,
 *          propertyNames, store/load round-trip via StringWriter/StringReader, and put/get
 *          (non-string values).
 * @run main PropertiesTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public class PropertiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
