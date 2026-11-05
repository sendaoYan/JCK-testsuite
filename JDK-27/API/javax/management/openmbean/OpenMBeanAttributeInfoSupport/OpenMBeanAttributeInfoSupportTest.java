/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanAttributeInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanAttributeInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanAttributeInfo.class.isAssignableFrom(c), "extends MBeanAttributeInfo");
        check(OpenMBeanAttributeInfo.class.isAssignableFrom(c), "implements OpenMBeanAttributeInfo");
        check(c.getMethod("isReadable") != null, "has isReadable");
        check(c.getMethod("isWritable") != null, "has isWritable");
        check(c.getMethod("isIs") != null, "has isIs");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanAttributeInfoSupport ai = new OpenMBeanAttributeInfoSupport(
            "Name", "desc", SimpleType.STRING, true, true, false);
        check(ai.getName().equals("Name"), "name");
        check(ai.getDescription().equals("desc"), "description");
        check(ai.getOpenType() == SimpleType.STRING, "openType");
        check(ai.isReadable(), "isReadable");
        check(ai.isWritable(), "isWritable");
        check(!ai.isIs(), "isIs false");
        check(!ai.hasDefaultValue() && !ai.hasLegalValues() && !ai.hasMinValue() && !ai.hasMaxValue(), "no constraints");
        check(ai.isValue("hello"), "isValue string");
        check(!ai.isValue(5), "reject integer");

        // isIs flag propagates
        OpenMBeanAttributeInfoSupport aiIs = new OpenMBeanAttributeInfoSupport(
            "Active", "d", SimpleType.BOOLEAN, true, false, true);
        check(aiIs.isIs(), "isIs true");
        check(aiIs.isReadable() && !aiIs.isWritable(), "readable not writable");

        // default value variant
        OpenMBeanAttributeInfoSupport aiDef = new OpenMBeanAttributeInfoSupport(
            "N", "d", SimpleType.INTEGER, true, true, false, 42);
        check(aiDef.hasDefaultValue() && aiDef.getDefaultValue().equals(42), "default value");
        check(aiDef.isValue(null), "null value allowed when default set");

        // equals / hashCode
        OpenMBeanAttributeInfoSupport ai2 = new OpenMBeanAttributeInfoSupport(
            "Name", "desc", SimpleType.STRING, true, true, false);
        check(ai.equals(ai2), "equals");
        check(ai.hashCode() == ai2.hashCode(), "hashCode equal");
        check(!ai.equals(null), "equals null false");
        check(!ai.equals("x"), "equals non-info false");
        check(!ai.equals(new OpenMBeanAttributeInfoSupport("Other", "desc", SimpleType.STRING, true, true, false)), "different name");
        check(!ai.equals(new OpenMBeanAttributeInfoSupport("Name", "desc", SimpleType.STRING, false, true, false)), "different readable");

        check(ai.toString().startsWith("javax.management.openmbean.OpenMBeanAttributeInfoSupport("), "toString prefix");
        check(ai.toString().contains("name=Name"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: batch g ----
        testBasicCtorG();
        testIsIsValidationG();
        testDefaultValueFormG();
        testLegalAndMinMaxFormsG();
        testValidationErrorsG();
        testEqualsHashCodeG();
        testCrossClassEqualsG();
        testDescriptorCtorG();
        testToStringShapeG();
        testSerializationG();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepened (batch g): full contract coverage =================

    interface ThrowingCall { void run() throws Throwable; }
    static void checkThrowsMsg(Class<? extends Throwable> type, String exactMessage, ThrowingCall r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (Throwable e) {
            if (e instanceof AssertionError) throw (AssertionError) e;
            check(type.isInstance(e), label + ": expected " + type.getSimpleName() + " but got " + e.getClass().getName());
            if (exactMessage != null)
                check(exactMessage.equals(e.getMessage()),
                    label + ": message <" + e.getMessage() + "> != <" + exactMessage + ">");
        }
    }
    @SuppressWarnings("rawtypes")
    static final OpenType RAW_INT = SimpleType.INTEGER;
    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    static void testBasicCtorG() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "desc", SimpleType.INTEGER, true, false, false);
        check("a".equals(a.getName()), "basic name");
        check("desc".equals(a.getDescription()), "basic desc");
        check(a.isReadable(), "basic readable");
        check(!a.isWritable(), "basic not writable");
        check(!a.isIs(), "basic not is");
        check(a.getOpenType() == SimpleType.INTEGER, "basic openType singleton");
        check("java.lang.Integer".equals(a.getType()), "getType is openType class name");
        check(!a.hasDefaultValue() && !a.hasLegalValues() && !a.hasMinValue() && !a.hasMaxValue(), "basic no constraints");
        check(a.getDefaultValue() == null && a.getLegalValues() == null && a.getMinValue() == null && a.getMaxValue() == null,
            "basic constraint getters null");
        check(a.isValue(1), "isValue Integer");
        check(!a.isValue("x"), "isValue reject String");
        check(!a.isValue(null), "isValue reject null");
        OpenMBeanAttributeInfoSupport w = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, false, true, false);
        check(!w.isReadable() && w.isWritable(), "write-only flags");
        OpenMBeanAttributeInfoSupport rw = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false);
        check(rw.isReadable() && rw.isWritable(), "read-write flags");
        OpenMBeanAttributeInfoSupport is = new OpenMBeanAttributeInfoSupport("ready", "d", SimpleType.BOOLEAN, true, false, true);
        check(is.isIs() && is.isReadable(), "isIs flags");
        check("java.lang.Boolean".equals(is.getType()), "isIs type Boolean");
    }

    static void testIsIsValidationG() throws Exception {
        checkThrowsMsg(IllegalArgumentException.class, "Cannot have an \"is\" getter for a non-readable attribute",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.BOOLEAN, false, true, true); } },
            "isIs without readable");
        checkThrowsMsg(IllegalArgumentException.class, "Cannot have an \"is\" getter for a non-boolean attribute",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, false, true); } },
            "isIs on non-boolean openType");
    }

    static void testDefaultValueFormG() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 3);
        check(a.hasDefaultValue(), "default form has default");
        check(Integer.valueOf(3).equals(a.getDefaultValue()), "default form value");
        check(a.isValue(null), "isValue null true with default");
        check(a.isValue(3), "isValue default");
        check(!a.hasMinValue() && !a.hasMaxValue() && !a.hasLegalValues(), "default form other constraints");
        check(Integer.valueOf(3).equals(a.getDescriptor().getFieldValue("defaultValue")), "descriptor defaultValue");
        OpenMBeanAttributeInfoSupport an = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, (Integer) null);
        check(!an.hasDefaultValue(), "null default not registered");
        check(an.getDefaultValue() == null, "null default value");
        check(!an.isValue(null), "isValue null false with null default");
    }

    static void testLegalAndMinMaxFormsG() throws Exception {
        OpenMBeanAttributeInfoSupport l = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 2, new Integer[] {1, 2, 3});
        check(l.hasLegalValues(), "legal form has");
        check(l.getLegalValues().size() == 3, "legal size");
        check(l.getLegalValues().contains(1) && l.getLegalValues().contains(3), "legal contents");
        check(l.isValue(1) && !l.isValue(99), "legal isValue bounds");
        try {
            @SuppressWarnings("unchecked")
            java.util.Set<Object> raw = (java.util.Set<Object>) l.getLegalValues();
            raw.add(4);
            throw new AssertionError("legal set should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "legal set unmodifiable");
        }
        check(l.getLegalValues() == l.getLegalValues(), "legal set stable reference");
        OpenMBeanAttributeInfoSupport m = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        check(m.hasMinValue() && Integer.valueOf(1).equals(m.getMinValue()), "min form min");
        check(m.hasMaxValue() && Integer.valueOf(10).equals(m.getMaxValue()), "min form max");
        check(m.isValue(1) && m.isValue(10), "min/max boundaries");
        check(!m.isValue(0) && !m.isValue(11), "outside min/max");
        check(!m.hasLegalValues(), "min form no legal");
        check(Integer.valueOf(1).equals(m.getDescriptor().getFieldValue("minValue")), "descriptor minValue");
        check(Integer.valueOf(10).equals(m.getDescriptor().getFieldValue("maxValue")), "descriptor maxValue");
    }

    static void testValidationErrorsG() throws Exception {
        checkThrowsMsg(OpenDataException.class,
            "Argument defaultValue's class [\"java.lang.String\"] does not match the one defined in openType[\"java.lang.Integer\"]",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("p", "d", RAW_INT, true, true, false, "str"); } },
            "default type mismatch");
        checkThrowsMsg(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport(null, "d", SimpleType.INTEGER, true, true, false); } },
            "null name");
        checkThrowsMsg(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("", "d", SimpleType.INTEGER, true, true, false); } },
            "empty name");
        checkThrowsMsg(IllegalArgumentException.class, "Description cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", null, SimpleType.INTEGER, true, true, false); } },
            "null description");
        checkThrowsMsg(IllegalArgumentException.class, "OpenType cannot be null",
            new ThrowingCall() { public void run() throws Exception {
                OpenType t = null; new OpenMBeanAttributeInfoSupport("n", "d", t, true, true, false); } },
            "null openType");
        checkThrowsMsg(OpenDataException.class, "minValue cannot be greater than defaultValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 0, 1, 10); } },
            "default below min");
        checkThrowsMsg(OpenDataException.class, "maxValue cannot be less than defaultValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 99, 1, 10); } },
            "default above max");
        checkThrowsMsg(OpenDataException.class, "minValue cannot be greater than maxValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, (Integer) null, 20, 10); } },
            "min greater than max");
        checkThrowsMsg(OpenDataException.class, "defaultValue is not contained in legalValues",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 99, new Integer[] {1, 2, 3}); } },
            "default not in legal");
        checkThrowsMsg(OpenDataException.class,
            "Element of legalValues [x] is not a valid value for the specified openType [javax.management.openmbean.SimpleType(name=java.lang.Integer)]",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", RAW_INT, true, true, false, 1, new Object[] {1, "x"}); } },
            "legal element wrong type");
    }

    static void testEqualsHashCodeG() throws Exception {
        OpenMBeanAttributeInfoSupport r = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, false, false);
        OpenMBeanAttributeInfoSupport w = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, false, true, false);
        check(!r.equals(w), "read/write flags participate");
        check(r.equals(new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, false, false)), "same flags equal");
        check(r.hashCode() == new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, false, false).hashCode(), "hashCode consistent");
        check(!r.equals(null), "equals null false");
        check(!r.equals("x"), "equals unrelated false");
        check(r.equals(r), "equals reflexive");
        check(r.equals(new OpenMBeanAttributeInfoSupport("n", "OTHER", SimpleType.INTEGER, true, false, false)), "description not in equals");
        check(!r.equals(new OpenMBeanAttributeInfoSupport("q", "d", SimpleType.INTEGER, true, false, false)), "name participates");
        check(!r.equals(new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.LONG, true, false, false)), "openType participates");
        OpenMBeanAttributeInfoSupport d1 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 1);
        OpenMBeanAttributeInfoSupport d2 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 2);
        check(!d1.equals(d2), "default participates");
        check(!d2.equals(d1), "default participates symmetric");
        OpenMBeanAttributeInfoSupport m1 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        check(!m1.equals(new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 5, 2, 10)), "min participates");
        check(m1.equals(new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10)), "same min/max equal");
        javax.management.Descriptor dv = new javax.management.ImmutableDescriptor(new String[] {"k"}, new Object[] {"v"});
        javax.management.Descriptor dw = new javax.management.ImmutableDescriptor(new String[] {"k"}, new Object[] {"w"});
        OpenMBeanAttributeInfoSupport c1 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, dv);
        OpenMBeanAttributeInfoSupport c2 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, dw);
        OpenMBeanAttributeInfoSupport c3 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, dv);
        check(!c1.equals(c2), "descriptor participates");
        check(c1.equals(c3), "same descriptor equal");
        OpenMBeanAttributeInfo viaIface = r;
        check(viaIface.isReadable() && !viaIface.isWritable() && !viaIface.isIs(), "flags via interface");
        check(viaIface.equals(r), "equals via interface");
    }

    static void testCrossClassEqualsG() throws Exception {
        OpenMBeanAttributeInfoSupport attr = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false);
        OpenMBeanParameterInfoSupport param = new OpenMBeanParameterInfoSupport("n", "d", SimpleType.INTEGER);
        check(!attr.equals(param), "attribute.equals(parameter) is false (attribute demands OpenMBeanAttributeInfo)");
        check(param.equals(attr), "parameter.equals(attribute) is true (only compares name/openType/defaults) - locked asymmetry");
    }

    static void testDescriptorCtorG() throws Exception {
        javax.management.Descriptor d = new javax.management.ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, d);
        check("customValue".equals(a.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(a.isReadable() && a.isWritable(), "descriptor form flags");
        check(a.getOpenType() == SimpleType.INTEGER, "descriptor form openType");
    }

    static void testToStringShapeG() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        String s = a.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanAttributeInfoSupport("), "toString prefix");
        check(s.contains("name=a"), "toString name");
        check(s.contains("default=5"), "toString default");
        check(s.contains("minValue=1") && s.contains("maxValue=10"), "toString min/max");
        check(s.contains("legalValues=null"), "toString legal null");
    }

    static void testSerializationG() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        OpenMBeanAttributeInfoSupport q = (OpenMBeanAttributeInfoSupport) roundTrip(a);
        check(a.equals(q), "round-trip equals");
        check(q.hashCode() == a.hashCode(), "round-trip hashCode");
        check(q.isReadable() && q.isWritable() && !q.isIs(), "round-trip flags");
        check(q.getOpenType() == SimpleType.INTEGER, "round-trip openType");
        check(Integer.valueOf(1).equals(q.getMinValue()) && Integer.valueOf(10).equals(q.getMaxValue()), "round-trip min/max");
        check(q.isValue(7) && !q.isValue(77), "round-trip isValue");
        OpenMBeanAttributeInfoSupport is = new OpenMBeanAttributeInfoSupport("ready", "d", SimpleType.BOOLEAN, true, false, true);
        OpenMBeanAttributeInfoSupport isq = (OpenMBeanAttributeInfoSupport) roundTrip(is);
        check(isq.isIs(), "round-trip isIs");
        check(java.io.ObjectStreamClass.lookup(OpenMBeanAttributeInfoSupport.class).getSerialVersionUID()
            == -4867215622149721849L, "serialVersionUID");
    }
}
