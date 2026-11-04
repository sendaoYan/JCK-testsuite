/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanParameterInfoSupportTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanParameterInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void checkThrows(java.lang.Class<? extends Throwable> type, String exactMessage, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + type.getSimpleName());
        } catch (Throwable e) {
            if (e instanceof AssertionError) throw (AssertionError) e;
            check(type.isInstance(e), label + ": type " + e.getClass().getName());
            if (exactMessage != null) check(exactMessage.equals(e.getMessage()),
                label + ": message <" + e.getMessage() + "> != <" + exactMessage + ">");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanParameterInfo.class.isAssignableFrom(c), "extends MBeanParameterInfo");
        check(OpenMBeanParameterInfo.class.isAssignableFrom(c), "implements OpenMBeanParameterInfo");
        check(c.getMethod("getOpenType") != null, "has getOpenType");
        check(c.getMethod("isValue", Object.class) != null, "has isValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void testBlackBox() throws Exception {
        // basic constructor
        OpenMBeanParameterInfoSupport pi = new OpenMBeanParameterInfoSupport("p", "desc", SimpleType.INTEGER);
        check(pi.getName().equals("p"), "name");
        check(pi.getDescription().equals("desc"), "description");
        check(pi.getOpenType() == SimpleType.INTEGER, "openType");
        check(!pi.hasDefaultValue(), "no default");
        check(!pi.hasLegalValues(), "no legal");
        check(!pi.hasMinValue(), "no min");
        check(!pi.hasMaxValue(), "no max");
        check(pi.getDefaultValue() == null, "default null");
        check(pi.getLegalValues() == null, "legal null");
        check(pi.getMinValue() == null, "min null");
        check(pi.getMaxValue() == null, "max null");
        check(pi.isValue(5), "isValue 5");
        check(pi.isValue(Integer.valueOf(7)), "isValue Integer");
        check(!pi.isValue("x"), "isValue reject string");
        check(!pi.isValue(null), "isValue reject null when no default");

        // default value constructor
        OpenMBeanParameterInfoSupport pd = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 3);
        check(pd.hasDefaultValue(), "has default");
        check(pd.getDefaultValue().equals(3), "default value");
        check(pd.isValue(null), "null isValue when default set");
        check(pd.isValue(3), "isValue default");

        // default + legal values constructor
        OpenMBeanParameterInfoSupport pl = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER,
            2, new Integer[]{1, 2, 3});
        check(pl.hasDefaultValue() && pl.getDefaultValue().equals(2), "legal default");
        check(pl.hasLegalValues(), "has legal");
        Set<?> legal = pl.getLegalValues();
        check(legal.size() == 3, "legal size");
        check(legal.contains(1) && legal.contains(2) && legal.contains(3), "legal contents");
        check(pl.isValue(1), "legal isValue 1");
        check(!pl.isValue(99), "legal reject 99");

        // min/max constructor
        OpenMBeanParameterInfoSupport pm = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER,
            5, 1, 10);
        check(pm.hasMinValue() && pm.getMinValue().equals(1), "min");
        check(pm.hasMaxValue() && pm.getMaxValue().equals(10), "max");
        check(pm.hasDefaultValue() && pm.getDefaultValue().equals(5), "minmax default");
        check(pm.isValue(1), "min boundary isValue");
        check(pm.isValue(10), "max boundary isValue");
        check(!pm.isValue(0), "below min rejected");
        check(!pm.isValue(11), "above max rejected");

        // equals / hashCode
        OpenMBeanParameterInfoSupport pi2 = new OpenMBeanParameterInfoSupport("p", "desc", SimpleType.INTEGER);
        check(pi.equals(pi2), "equals");
        check(pi.hashCode() == pi2.hashCode(), "hashCode equal");
        check(!pi.equals(null), "equals null false");
        check(!pi.equals("x"), "equals non-info false");
        check(!pi.equals(new OpenMBeanParameterInfoSupport("q", "desc", SimpleType.INTEGER)), "different name not equal");
        check(!pi.equals(new OpenMBeanParameterInfoSupport("p", "desc", SimpleType.STRING)), "different type not equal");

        // toString
        check(pi.toString().startsWith("javax.management.openmbean.OpenMBeanParameterInfoSupport("), "toString prefix");
        check(pi.toString().contains("name=p"), "toString name");

        // exceptions (exact message verified against JDK-27)
        checkThrows(OpenDataException.class, "Argument defaultValue's class [\"java.lang.String\"] does not match the one defined in openType[\"java.lang.Integer\"]",
            () -> { try { new OpenMBeanParameterInfoSupport("p", "d", (OpenType) SimpleType.INTEGER, "str"); }
                catch (Exception e) { throw sneaky(e); } }, "default type mismatch");
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E { throw (E) t; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
