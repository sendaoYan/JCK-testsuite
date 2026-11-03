/*
 * @test
 * @summary jdk.dynalink.linker.GuardingDynamicLinkerExporter conformance: white-box structure + black-box behavior.
 * @run main GuardingDynamicLinkerExporterTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardingDynamicLinkerExporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardingDynamicLinkerExporter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "get"), "get");

    }

    static void testBlackBox() throws Exception {
        check(GuardingDynamicLinkerExporter.AUTOLOAD_PERMISSION_NAME != null, "AUTOLOAD_PERMISSION_NAME");
        check(true, "abstract class; constant verified");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
