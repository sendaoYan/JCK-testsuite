/*
 * @test
 * @summary java.util.Properties API conformance: setProperty/getProperty with defaults,
 *          size/containsKey, and constructor-provided defaults.
 * @run main PropertiesTest
 */

import java.util.Properties;

public class PropertiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        Properties p = new Properties();
        p.setProperty("k", "v");
        check("v".equals(p.getProperty("k")), "setProperty/getProperty");
        check("default".equals(p.getProperty("missing", "default")), "getProperty with default");
        check(p.size() == 1 && p.containsKey("k"), "size/containsKey");
    }

    static void testDefaults() {
        Properties defs = new Properties();
        defs.setProperty("x", "X");
        Properties p = new Properties(defs);
        check("X".equals(p.getProperty("x")), "defaults via constructor");
        p.setProperty("x", "Y");
        check("Y".equals(p.getProperty("x")), "own value overrides default");
    }

    public static void main(String[] args) {
        testBasics();
        testDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
