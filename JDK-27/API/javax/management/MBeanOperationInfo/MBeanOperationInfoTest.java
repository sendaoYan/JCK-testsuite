/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanOperationInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanOperationInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getReturnType"), "has getReturnType");
        check(hasMethod(c, "getSignature"), "has getSignature");
        check(hasMethod(c, "getImpact"), "has getImpact");
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("p", "int", "p desc");
        MBeanOperationInfo op = new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION);
        check("Op".equals(op.getName()), "getName");
        check("op desc".equals(op.getDescription()), "getDescription");
        check("java.lang.String".equals(op.getReturnType()), "getReturnType");
        check(op.getImpact() == MBeanOperationInfo.ACTION, "getImpact");
        check(op.getSignature().length == 1 && "p".equals(op.getSignature()[0].getName()), "getSignature");

        // Method-based introspection (first arg is description; name is the method name)
        Method m = Sample.class.getMethod("echo", String.class);
        MBeanOperationInfo viaMethod = new MBeanOperationInfo("Echo", m);
        check("echo".equals(viaMethod.getName()), "method ctor name");
        check("java.lang.String".equals(viaMethod.getReturnType()), "method ctor return type");
        check(viaMethod.getSignature().length == 1, "method ctor signature length");
        check("java.lang.String".equals(viaMethod.getSignature()[0].getType()), "method ctor param type");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Op", "description=op desc");
        MBeanOperationInfo od = new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO, d);
        check(od.getDescriptor() != null && "Op".equals(od.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals
        check(op.equals(new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION)), "equals identical");
        check(!op.equals(new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "int", MBeanOperationInfo.ACTION)), "different return type not equal");
        check(!op.equals(new MBeanOperationInfo("Other", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION)), "different name not equal");
        check(!op.equals(null), "not equal null");
        check(op.hashCode() == new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION).hashCode(), "equal hashCode");

        // clone
        MBeanOperationInfo clone = (MBeanOperationInfo) op.clone();
        check(clone != op && clone.equals(op), "clone equals original");
        check(op.toString() != null, "toString non-null");
    }

    public static class Sample {
        public String echo(String s) { return s; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
