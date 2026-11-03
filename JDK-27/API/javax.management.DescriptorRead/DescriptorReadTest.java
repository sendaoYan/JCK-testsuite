/*
 * @test
 * @summary javax.management.DescriptorRead conformance: white-box structure + light behavior.
 * @run main DescriptorReadTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class DescriptorReadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorRead");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getDescriptor"), "has getDescriptor");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final Descriptor d = new ImmutableDescriptor("name=n");
        DescriptorRead read = new DescriptorRead() {
            public Descriptor getDescriptor() { return d; }
        };
        check(read.getDescriptor() == d, "getDescriptor returns descriptor");

        // MBeanFeatureInfo implements DescriptorRead
        MBeanFeatureInfo fi = new MBeanFeatureInfo("n", "d");
        check(fi.getDescriptor() != null, "MBeanFeatureInfo.getDescriptor non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
