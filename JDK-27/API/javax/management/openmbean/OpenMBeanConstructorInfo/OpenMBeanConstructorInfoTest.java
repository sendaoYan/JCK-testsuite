/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanConstructorInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class OpenMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getSignature"), "getSignature");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfo[] sig = { new OpenMBeanParameterInfoSupport("p", "param", SimpleType.INTEGER) };
        OpenMBeanConstructorInfo ci = new OpenMBeanConstructorInfoSupport("ctor", "constructor", sig);
        check(ci.getName().equals("ctor"), "name");
        check(ci.getDescription().equals("constructor"), "description");
        check(ci.getSignature().length == 1, "signature length");
        check(ci.getSignature()[0].getName().equals("p"), "signature param name");
        check(ci.equals(ci), "equals");
        check(ci.hashCode() == ci.hashCode(), "hashCode");
        check(ci.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
