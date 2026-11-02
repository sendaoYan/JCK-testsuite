/*
 * @test
 * @summary java.beans.MethodDescriptor conformance: method + parameter descriptors.
 * @run main MethodDescriptorTest
 */

import java.beans.*;

public class MethodDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class MyBean { public String getName() { return null; } }

    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method m = MyBean.class.getMethod("getName");
        MethodDescriptor md = new MethodDescriptor(m);
        check(md.getMethod() == m, "method");
        check(md.getName().equals("getName"), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
