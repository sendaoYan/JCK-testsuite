/*
 * @test
 * @summary java.beans.MethodDescriptor conformance: wrap a Method and parameter descriptors.
 * @run main MethodDescriptorTest
 */

import java.beans.*;
import java.lang.reflect.Method;

public class MethodDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Method m = String.class.getMethod("indexOf", String.class);
        MethodDescriptor md = new MethodDescriptor(m);
        check(md.getMethod() == m, "method");
        check("indexOf".equals(md.getName()), "name");
        ParameterDescriptor[] pds = new ParameterDescriptor[]{ new ParameterDescriptor() };
        MethodDescriptor md2 = new MethodDescriptor(m, pds);
        check(md2.getParameterDescriptors() != null && md2.getParameterDescriptors().length == 1, "parameter descriptors");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
