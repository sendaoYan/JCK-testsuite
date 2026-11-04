/*
 * @test
 * @summary javax.tools.JavaFileManager.Location conformance: white-box structure + black-box behavior.
 * @run main LocationTest
 */

import java.lang.reflect.*;

public class LocationTest {
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
        Class<?> c = Class.forName("javax.tools.JavaFileManager$Location");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isOutputLocation"), "declares isOutputLocation");
        check(hasMethod(c, "isModuleOrientedLocation"), "declares isModuleOrientedLocation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // StandardLocation is the canonical implementation of Location.
        javax.tools.JavaFileManager.Location loc = javax.tools.StandardLocation.CLASS_OUTPUT;
        check("CLASS_OUTPUT".equals(loc.getName()), "CLASS_OUTPUT getName");
        check(loc.isOutputLocation(), "CLASS_OUTPUT is output");
        check(!loc.isModuleOrientedLocation(), "CLASS_OUTPUT not module oriented");

        javax.tools.JavaFileManager.Location sp = javax.tools.StandardLocation.SOURCE_PATH;
        check("SOURCE_PATH".equals(sp.getName()), "SOURCE_PATH getName");
        check(!sp.isOutputLocation(), "SOURCE_PATH not output");
        check(!sp.isModuleOrientedLocation(), "SOURCE_PATH not module oriented");

        javax.tools.JavaFileManager.Location mp = javax.tools.StandardLocation.MODULE_PATH;
        check("MODULE_PATH".equals(mp.getName()), "MODULE_PATH getName");
        check(mp.isModuleOrientedLocation(), "MODULE_PATH module oriented");

        // DocumentationTool.Location is also a Location.
        javax.tools.JavaFileManager.Location doc = javax.tools.DocumentationTool.Location.DOCUMENTATION_OUTPUT;
        check("DOCUMENTATION_OUTPUT".equals(doc.getName()), "DOCUMENTATION_OUTPUT getName");
        check(doc.isOutputLocation(), "DOCUMENTATION_OUTPUT is output");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
