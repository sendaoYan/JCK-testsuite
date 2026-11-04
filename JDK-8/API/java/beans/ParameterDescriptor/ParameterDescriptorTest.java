/*
 * @test
 * @summary java.beans.ParameterDescriptor conformance: default constructor.
 * @run main ParameterDescriptorTest
 */

import java.beans.*;
public class ParameterDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("arg");
        check("arg".equals(pd.getName()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
