/*
 * @test
 * @summary java.beans.FeatureDescriptor conformance: white-box structure + black-box behavior.
 * @run main FeatureDescriptorTest
 */

import java.beans.FeatureDescriptor;
import java.util.Enumeration;

public class FeatureDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.FeatureDescriptor");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName("n"); check("n".equals(fd.getName()), "name");
        fd.setDisplayName("dn"); check("dn".equals(fd.getDisplayName()), "displayName");
        fd.setShortDescription("desc"); check("desc".equals(fd.getShortDescription()), "shortDescription");
        check(!fd.isExpert(), "expert default false"); fd.setExpert(true); check(fd.isExpert(), "expert");
        check(!fd.isHidden(), "hidden default false"); fd.setHidden(true); check(fd.isHidden(), "hidden");
        check(!fd.isPreferred(), "preferred default false");
        fd.setPreferred(true); check(fd.isPreferred(), "preferred true");

        fd.setValue("k", "v");
        check("v".equals(fd.getValue("k")), "getValue/setValue");
        Enumeration<String> en = fd.attributeNames();
        check(en != null && en.hasMoreElements(), "attributeNames");
        check("k".equals(en.nextElement()), "attributeNames first");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
