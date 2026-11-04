/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanOperationInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class OpenMBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getSignature"), "getSignature");
        check(hasMethod(c, "getImpact"), "getImpact");
        check(hasMethod(c, "getReturnType"), "getReturnType");
        check(hasMethod(c, "getReturnOpenType"), "getReturnOpenType");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanOperationInfo oi = new OpenMBeanOperationInfoSupport("op", "operation", new OpenMBeanParameterInfo[0], SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.getName().equals("op"), "name");
        check(oi.getDescription().equals("operation"), "description");
        check(oi.getSignature().length == 0, "signature empty");
        check(oi.getImpact() == MBeanOperationInfo.INFO, "impact");
        check(oi.getReturnOpenType() == SimpleType.INTEGER, "return open type");
        check(oi.getReturnType().equals("java.lang.Integer"), "return type name");
        check(oi.equals(oi), "equals");
        check(oi.hashCode() == oi.hashCode(), "hashCode");
        check(oi.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
