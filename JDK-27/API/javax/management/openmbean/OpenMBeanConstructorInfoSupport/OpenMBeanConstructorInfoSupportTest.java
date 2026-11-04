/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanConstructorInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanConstructorInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanConstructorInfo.class.isAssignableFrom(c), "extends MBeanConstructorInfo");
        check(OpenMBeanConstructorInfo.class.isAssignableFrom(c), "implements OpenMBeanConstructorInfo");
        check(c.getMethod("getSignature") != null, "has getSignature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfoSupport[] params = new OpenMBeanParameterInfoSupport[]{
            new OpenMBeanParameterInfoSupport("p", "param desc", SimpleType.STRING)
        };
        OpenMBeanConstructorInfoSupport ci = new OpenMBeanConstructorInfoSupport("Test", "ctor desc", params);
        check(ci.getName().equals("Test"), "name");
        check(ci.getDescription().equals("ctor desc"), "description");
        MBeanParameterInfo[] sig = ci.getSignature();
        check(sig.length == 1, "signature length");
        check(sig[0].getName().equals("p"), "param name");
        check(sig[0] instanceof OpenMBeanParameterInfoSupport, "param is OpenMBeanParameterInfoSupport");
        check(((OpenMBeanParameterInfoSupport) sig[0]).getOpenType() == SimpleType.STRING, "param openType");

        // empty signature
        OpenMBeanConstructorInfoSupport empty = new OpenMBeanConstructorInfoSupport("Empty", "d", new OpenMBeanParameterInfoSupport[0]);
        check(empty.getSignature().length == 0, "empty signature");

        // equals / hashCode
        OpenMBeanConstructorInfoSupport ci2 = new OpenMBeanConstructorInfoSupport("Test", "ctor desc", params);
        check(ci.equals(ci2), "equals");
        check(ci.hashCode() == ci2.hashCode(), "hashCode equal");
        check(!ci.equals(null), "equals null false");
        check(!ci.equals("x"), "equals non-info false");
        check(!ci.equals(new OpenMBeanConstructorInfoSupport("Other", "ctor desc", params)), "different name");
        check(!ci.equals(empty), "different signature");

        check(ci.toString().startsWith("javax.management.openmbean.OpenMBeanConstructorInfoSupport("), "toString prefix");
        check(ci.toString().contains("name=Test"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
