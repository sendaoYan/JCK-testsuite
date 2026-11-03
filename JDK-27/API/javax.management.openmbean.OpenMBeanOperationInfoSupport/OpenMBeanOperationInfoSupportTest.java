/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanOperationInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanOperationInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanOperationInfo.class.isAssignableFrom(c), "extends MBeanOperationInfo");
        check(OpenMBeanOperationInfo.class.isAssignableFrom(c), "implements OpenMBeanOperationInfo");
        check(c.getMethod("getReturnOpenType") != null, "has getReturnOpenType");
        check(c.getMethod("getImpact") != null, "has getImpact");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfoSupport[] params = new OpenMBeanParameterInfoSupport[]{
            new OpenMBeanParameterInfoSupport("x", "x desc", SimpleType.INTEGER)
        };
        OpenMBeanOperationInfoSupport oi = new OpenMBeanOperationInfoSupport(
            "add", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.getName().equals("add"), "name");
        check(oi.getDescription().equals("add desc"), "description");
        check(oi.getReturnOpenType() == SimpleType.INTEGER, "return open type");
        check(oi.getReturnType().equals("java.lang.Integer"), "return type class name");
        check(oi.getImpact() == MBeanOperationInfo.INFO, "impact INFO");
        MBeanParameterInfo[] sig = oi.getSignature();
        check(sig.length == 1 && sig[0].getName().equals("x"), "signature");
        check(((OpenMBeanParameterInfoSupport) sig[0]).getOpenType() == SimpleType.INTEGER, "param open type");

        // different impact constants
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.ACTION).getImpact() == MBeanOperationInfo.ACTION, "impact ACTION");
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.ACTION_INFO).getImpact() == MBeanOperationInfo.ACTION_INFO, "impact ACTION_INFO");
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.UNKNOWN).getImpact() == MBeanOperationInfo.UNKNOWN, "impact UNKNOWN");

        // equals / hashCode
        OpenMBeanOperationInfoSupport oi2 = new OpenMBeanOperationInfoSupport(
            "add", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.equals(oi2), "equals");
        check(oi.hashCode() == oi2.hashCode(), "hashCode equal");
        check(!oi.equals(null), "equals null false");
        check(!oi.equals("x"), "equals non-info false");
        check(!oi.equals(new OpenMBeanOperationInfoSupport("sub", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO)), "different name");
        check(!oi.equals(new OpenMBeanOperationInfoSupport("add", "add desc", params, SimpleType.LONG, MBeanOperationInfo.INFO)), "different return type");

        check(oi.toString().startsWith("javax.management.openmbean.OpenMBeanOperationInfoSupport("), "toString prefix");
        check(oi.toString().contains("name=add"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
