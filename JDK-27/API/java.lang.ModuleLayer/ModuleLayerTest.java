/*
 * @test
 * @summary java.lang.ModuleLayer conformance: white-box structure + black-box behavior.
 * @run main ModuleLayerTest
 */

import java.lang.ModuleLayer;
import java.lang.reflect.Modifier;

public class ModuleLayerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ModuleLayer");
        check(Modifier.isFinal(c.getModifiers()), "ModuleLayer is final");
        check(!c.isInterface(), "ModuleLayer is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBoot() {
        ModuleLayer boot = ModuleLayer.boot();
        check(boot != null, "boot non-null");
        check(boot.findModule("java.base").isPresent(), "boot has java.base");
        check(boot.modules().size() > 0, "boot modules non-empty");
        check(boot.configuration().modules().size() > 0, "boot configuration non-empty");
        check(boot.findLoader("java.base") == null, "findLoader java.base bootstrap (null)");
        check(boot.parents().size() == 1, "boot layer has exactly one parent (the empty layer)");
        check(boot.parents().get(0).modules().isEmpty(), "boot layer's parent is the empty layer");
        check(boot.toString() != null && !boot.toString().isEmpty(), "toString non-empty");
    }

    static void testEmpty() {
        ModuleLayer empty = ModuleLayer.empty();
        check(empty.modules().isEmpty(), "empty layer has no modules");
        check(empty.configuration().modules().isEmpty(), "empty configuration");
        check(!empty.findModule("java.base").isPresent(), "empty layer lacks java.base");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBoot();
        testEmpty();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
