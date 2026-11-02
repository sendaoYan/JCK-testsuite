/*
 * @test
 * @summary java.beans.FeatureDescriptor conformance: name/display/expert/hidden/preferred/value.
 * @run main FeatureDescriptorTest
 */

import java.beans.*;

public class FeatureDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("n"); check("n".equals(fd.getName()), "name");
        fd.setDisplayName("d"); check("d".equals(fd.getDisplayName()), "display name");
        fd.setShortDescription("s"); check("s".equals(fd.getShortDescription()), "short desc");
        fd.setExpert(true); check(fd.isExpert(), "expert");
        fd.setHidden(true); check(fd.isHidden(), "hidden");
        fd.setPreferred(true); check(fd.isPreferred(), "preferred");
        fd.setValue("k", "v"); check("v".equals(fd.getValue("k")), "value");
        check(fd.attributeNames() != null, "attribute names");
        check(fd.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
