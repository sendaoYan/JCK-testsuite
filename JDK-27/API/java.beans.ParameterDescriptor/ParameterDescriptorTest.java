/*
 * @test
 * @summary java.beans.ParameterDescriptor conformance: white-box structure + black-box behavior.
 * @run main ParameterDescriptorTest
 */

import java.beans.ParameterDescriptor;

public class ParameterDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ParameterDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
    }

    static void testBlackBox() throws Exception {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("p");
        check("p".equals(pd.getName()), "name via FeatureDescriptor");
        pd.setShortDescription("param desc");
        check("param desc".equals(pd.getShortDescription()), "shortDescription");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
