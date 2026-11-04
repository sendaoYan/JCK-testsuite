/*
 * @test
 * @summary javax.management.MBeanFeatureInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanFeatureInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class MBeanFeatureInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanFeatureInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(DescriptorRead.class.isAssignableFrom(c), "implements DescriptorRead");
        check(hasMethod(c, "getName"), "has getName");
        check(hasMethod(c, "getDescription"), "has getDescription");
        check(hasMethod(c, "getDescriptor"), "has getDescriptor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanFeatureInfo info = new MBeanFeatureInfo("name", "description");
        check("name".equals(info.getName()), "getName");
        check("description".equals(info.getDescription()), "getDescription");
        check(info.getDescriptor() != null, "getDescriptor non-null");
        check(info.getDescriptor().getFieldNames().length == 0, "default descriptor is empty");

        // descriptor constructor preserves the provided descriptor
        ImmutableDescriptor desc = new ImmutableDescriptor("name=n", "description=d", "extra=e");
        MBeanFeatureInfo info2 = new MBeanFeatureInfo("n", "d", desc);
        check("n".equals(info2.getName()) && "d".equals(info2.getDescription()), "name/description from ctor");
        check(desc.equals(info2.getDescriptor()), "descriptor preserved");
        check("e".equals(info2.getDescriptor().getFieldValue("extra")), "extra descriptor field");

        // equals compares name and description
        check(info.equals(new MBeanFeatureInfo("name", "description")), "equals same fields");
        check(!info.equals(new MBeanFeatureInfo("other", "description")), "different name not equal");
        check(!info.equals(new MBeanFeatureInfo("name", "other")), "different description not equal");
        check(!info.equals(null), "not equal null");
        check(info.hashCode() == new MBeanFeatureInfo("name", "description").hashCode(), "equal hashCode");

        // equals on descriptor-bearing instances
        check(info2.equals(new MBeanFeatureInfo("n", "d", desc)), "equals with descriptor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
