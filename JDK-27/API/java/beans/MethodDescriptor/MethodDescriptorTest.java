/*
 * @test
 * @summary java.beans.MethodDescriptor conformance: white-box structure + black-box behavior.
 * @run main MethodDescriptorTest
 */

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.lang.reflect.Method;

public class MethodDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.MethodDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
    }

    static void testBlackBox() throws Exception {
        Method m = String.class.getMethod("substring", int.class);
        MethodDescriptor md = new MethodDescriptor(m);
        check(m == md.getMethod(), "method");

        Method m2 = String.class.getMethod("substring", int.class, int.class);
        ParameterDescriptor[] pd = new ParameterDescriptor[]{ new ParameterDescriptor(), new ParameterDescriptor() };
        MethodDescriptor md2 = new MethodDescriptor(m2, pd);
        check(m2 == md2.getMethod(), "method2");
        check(md2.getParameterDescriptors() != null && md2.getParameterDescriptors().length == 2, "parameterDescriptors");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
