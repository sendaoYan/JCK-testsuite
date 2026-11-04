/*
 * @test
 * @summary javax.management.MBeanParameterInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanParameterInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanParameterInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getType"), "has getType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("name", "java.lang.String", "desc");
        check("name".equals(p.getName()), "getName");
        check("java.lang.String".equals(p.getType()), "getType");
        check("desc".equals(p.getDescription()), "getDescription");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=name", "description=desc");
        MBeanParameterInfo pd = new MBeanParameterInfo("name", "java.lang.String", "desc", d);
        check(pd.getDescriptor() != null && "name".equals(pd.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals compares name + type
        check(p.equals(new MBeanParameterInfo("name", "java.lang.String", "desc")), "equals identical");
        check(!p.equals(new MBeanParameterInfo("name", "int", "desc")), "different type not equal");
        check(!p.equals(new MBeanParameterInfo("other", "java.lang.String", "desc")), "different name not equal");
        check(!p.equals(null), "not equal null");
        check(p.hashCode() == new MBeanParameterInfo("name", "java.lang.String", "desc").hashCode(), "equal hashCode");

        // clone
        MBeanParameterInfo clone = (MBeanParameterInfo) p.clone();
        check(clone != p && clone.equals(p), "clone equals original");
        check(p.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
