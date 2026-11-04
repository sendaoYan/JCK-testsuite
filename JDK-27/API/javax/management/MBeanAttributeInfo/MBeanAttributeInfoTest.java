/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanAttributeInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanAttributeInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getType"), "has getType");
        check(hasMethod(c, "isReadable"), "has isReadable");
        check(hasMethod(c, "isWritable"), "has isWritable");
        check(hasMethod(c, "isIs"), "has isIs");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        check("Count".equals(a.getName()), "getName");
        check("int".equals(a.getType()), "getType");
        check("desc".equals(a.getDescription()), "getDescription");
        check(a.isReadable(), "isReadable");
        check(!a.isWritable(), "isWritable");
        check(!a.isIs(), "isIs");

        // isIs variant
        MBeanAttributeInfo isAttr = new MBeanAttributeInfo("Ready", "boolean", "d", true, false, true);
        check(isAttr.isIs(), "isIs true");
        check(isAttr.isReadable() && !isAttr.isWritable(), "is/isReadable/isWritable combo");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Count", "description=desc");
        MBeanAttributeInfo ad = new MBeanAttributeInfo("Count", "int", "desc", true, true, false, d);
        check(ad.getDescriptor() != null && "Count".equals(ad.getDescriptor().getFieldValue("name")),
                "descriptor ctor");

        // Method-based constructor (introspection)
        MBeanAttributeInfo viaMethod = new MBeanAttributeInfo("V", "desc", getMethod("getV"), getMethod("setV"));
        check("V".equals(viaMethod.getName()), "method ctor name");
        check(viaMethod.isReadable() && viaMethod.isWritable(), "method ctor readable/writable");

        // equals compares all attribute fields
        check(a.equals(new MBeanAttributeInfo("Count", "int", "desc", true, false, false)), "equals identical");
        check(!a.equals(new MBeanAttributeInfo("Count", "int", "desc", true, true, false)), "different writable not equal");
        check(!a.equals(new MBeanAttributeInfo("Count", "long", "desc", true, false, false)), "different type not equal");
        check(!a.equals(new MBeanAttributeInfo("Other", "int", "desc", true, false, false)), "different name not equal");
        check(!a.equals(null), "not equal null");
        check(a.hashCode() == new MBeanAttributeInfo("Count", "int", "desc", true, false, false).hashCode(), "equal hashCode");

        // clone
        MBeanAttributeInfo clone = (MBeanAttributeInfo) a.clone();
        check(clone != a && clone.equals(a), "clone equals original");

        check(a.toString() != null, "toString non-null");
    }

    static Method getMethod(String name) throws Exception {
        for (Method m : Sample.class.getMethods()) if (m.getName().equals(name)) return m;
        throw new NoSuchMethodException(name);
    }
    public static class Sample {
        public int getV() { return 0; }
        public void setV(int v) { }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
