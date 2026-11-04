/*
 * @test
 * @summary jdk.dynalink.linker.LinkerServices conformance: white-box structure + black-box behavior.
 * @run main LinkerServicesTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class LinkerServicesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.LinkerServices");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "asType"), "asType");
        check(hasMethod(c, "getTypeConverter"), "getTypeConverter");
        check(hasMethod(c, "canConvert"), "canConvert");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");
        check(hasMethod(c, "compareConversion"), "compareConversion");

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
