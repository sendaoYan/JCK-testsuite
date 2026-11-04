/*
 * @test
 * @summary javax.tools.StandardLocation conformance: white-box structure + black-box behavior.
 * @run main StandardLocationTest
 */

import java.lang.reflect.*;

public class StandardLocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.StandardLocation");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 13, "13 enum constants");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager$Location"), "implements javax.tools.JavaFileManager$Location");
        check(hasMethod(c, "locationFor"), "declares locationFor");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isOutputLocation"), "declares isOutputLocation");
        check(hasMethod(c, "isModuleOrientedLocation"), "declares isModuleOrientedLocation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.tools.StandardLocation[] all = javax.tools.StandardLocation.values();
        String[] expected = {
            "CLASS_OUTPUT", "SOURCE_OUTPUT", "CLASS_PATH", "SOURCE_PATH",
            "ANNOTATION_PROCESSOR_PATH", "ANNOTATION_PROCESSOR_MODULE_PATH",
            "PLATFORM_CLASS_PATH", "NATIVE_HEADER_OUTPUT", "MODULE_SOURCE_PATH",
            "UPGRADE_MODULE_PATH", "SYSTEM_MODULES", "MODULE_PATH", "PATCH_MODULE_PATH"
        };
        check(all.length == expected.length, "values() length 13");
        for (int i = 0; i < expected.length; i++) {
            check(expected[i].equals(all[i].name()), "order/name " + expected[i]);
        }

        // getName == enum name.
        check("CLASS_OUTPUT".equals(javax.tools.StandardLocation.CLASS_OUTPUT.getName()), "getName CLASS_OUTPUT");
        check("MODULE_PATH".equals(javax.tools.StandardLocation.MODULE_PATH.getName()), "getName MODULE_PATH");

        // Output locations.
        check(javax.tools.StandardLocation.CLASS_OUTPUT.isOutputLocation(), "CLASS_OUTPUT output");
        check(javax.tools.StandardLocation.SOURCE_OUTPUT.isOutputLocation(), "SOURCE_OUTPUT output");
        check(javax.tools.StandardLocation.NATIVE_HEADER_OUTPUT.isOutputLocation(), "NATIVE_HEADER_OUTPUT output");
        check(!javax.tools.StandardLocation.CLASS_PATH.isOutputLocation(), "CLASS_PATH not output");
        check(!javax.tools.StandardLocation.SOURCE_PATH.isOutputLocation(), "SOURCE_PATH not output");

        // Module-oriented locations.
        check(javax.tools.StandardLocation.MODULE_SOURCE_PATH.isModuleOrientedLocation(), "MODULE_SOURCE_PATH module");
        check(javax.tools.StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH.isModuleOrientedLocation(),
                "ANNOTATION_PROCESSOR_MODULE_PATH module");
        check(javax.tools.StandardLocation.UPGRADE_MODULE_PATH.isModuleOrientedLocation(), "UPGRADE_MODULE_PATH module");
        check(javax.tools.StandardLocation.SYSTEM_MODULES.isModuleOrientedLocation(), "SYSTEM_MODULES module");
        check(javax.tools.StandardLocation.MODULE_PATH.isModuleOrientedLocation(), "MODULE_PATH module");
        check(javax.tools.StandardLocation.PATCH_MODULE_PATH.isModuleOrientedLocation(), "PATCH_MODULE_PATH module");
        check(!javax.tools.StandardLocation.CLASS_OUTPUT.isModuleOrientedLocation(), "CLASS_OUTPUT not module");

        // locationFor round-trips known names.
        check(javax.tools.StandardLocation.locationFor("CLASS_OUTPUT") == javax.tools.StandardLocation.CLASS_OUTPUT,
                "locationFor CLASS_OUTPUT");
        check(javax.tools.StandardLocation.locationFor("MODULE_PATH") == javax.tools.StandardLocation.MODULE_PATH,
                "locationFor MODULE_PATH");
        javax.tools.JavaFileManager.Location unknown = javax.tools.StandardLocation.locationFor("CUSTOM_OUTPUT");
        check("CUSTOM_OUTPUT".equals(unknown.getName()), "locationFor custom name");
        check(unknown.isOutputLocation(), "locationFor custom output");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
