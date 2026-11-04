/*
 * @test
 * @summary java.beans.SimpleBeanInfo conformance: default null/empty accessors.
 * @run main SimpleBeanInfoTest
 */

import java.beans.*;
public class SimpleBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        SimpleBeanInfo bi = new SimpleBeanInfo();
        check(bi.getBeanDescriptor() == null, "bean descriptor null");
        check(bi.getPropertyDescriptors() == null, "property descriptors null");
        check(bi.getMethodDescriptors() == null, "method descriptors null");
        check(bi.getDefaultPropertyIndex() == -1, "default property index");
        check(bi.getDefaultEventIndex() == -1, "default event index");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
