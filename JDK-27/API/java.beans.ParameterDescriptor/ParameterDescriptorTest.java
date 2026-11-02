/*
 * @test
 * @summary java.beans.ParameterDescriptor conformance: name and display name.
 * @run main ParameterDescriptorTest
 */

import java.beans.*;

public class ParameterDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        ParameterDescriptor pd = new ParameterDescriptor();
        pd.setName("arg0"); check("arg0".equals(pd.getName()), "name");
        pd.setDisplayName("Arg"); check("Arg".equals(pd.getDisplayName()), "display name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
