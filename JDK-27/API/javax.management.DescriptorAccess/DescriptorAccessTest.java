/*
 * @test
 * @summary javax.management.DescriptorAccess conformance: white-box structure + light behavior.
 * @run main DescriptorAccessTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class DescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorAccess");
        check(c.isInterface(), "is interface");
        check(DescriptorRead.class.isAssignableFrom(c), "extends DescriptorRead");
        check(hasMethod(c, "setDescriptor"), "has setDescriptor");
        check(hasMethod(c, "getDescriptor"), "has getDescriptor (from DescriptorRead)");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final Descriptor[] current = { null };
        DescriptorAccess access = new DescriptorAccess() {
            public Descriptor getDescriptor() { return current[0]; }
            public void setDescriptor(Descriptor d) { current[0] = d; }
        };
        Descriptor d = new ImmutableDescriptor("name=n");
        access.setDescriptor(d);
        check(access.getDescriptor() == d, "set/getDescriptor round-trip");

        // StandardMBean is a DescriptorAccess? MBeanFeatureInfo is a DescriptorRead.
        MBeanFeatureInfo fi = new MBeanFeatureInfo("n", "d");
        check(fi instanceof DescriptorRead, "MBeanFeatureInfo is DescriptorRead");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
