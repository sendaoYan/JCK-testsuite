/*
 * @test
 * @summary java.beans.FeatureDescriptor conformance: name/display/short/values.
 * @run main FeatureDescriptorTest
 */

import java.beans.*;
public class FeatureDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("n");
        fd.setDisplayName("display");
        fd.setShortDescription("short");
        check("n".equals(fd.getName()), "name");
        check("display".equals(fd.getDisplayName()), "display name");
        check("short".equals(fd.getShortDescription()), "short description");
        fd.setValue("k", "v");
        check("v".equals(fd.getValue("k")), "value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
