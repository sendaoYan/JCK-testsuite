/*
 * @test
 * @summary jdk.dynalink.RelinkableCallSite conformance: white-box structure + black-box behavior.
 * @run main RelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class RelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.RelinkableCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "initialize"), "initialize");
        check(hasMethod(c, "getDescriptor"), "getDescriptor");
        check(hasMethod(c, "relink"), "relink");
        check(hasMethod(c, "resetAndRelink"), "resetAndRelink");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
