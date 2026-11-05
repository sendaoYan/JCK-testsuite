/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanAttributeInfoSupport conformance: ctor matrix, read/write/is flags, default/min/max/legal validation, equals incl. cross-class asymmetry, serialization.
 * @run main OpenMBeanAttributeInfoSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Set;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanAttributeInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    interface ThrowingCall { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> type, String exactMessage, ThrowingCall r, String label) {
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanAttributeInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanAttributeInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanAttributeInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanAttributeInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanAttributeInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getDefaultValue"), "declares getDefaultValue");
        check(hasMethod(c, "getLegalValues"), "declares getLegalValues");
        // ---- deepened: batch g ----
        testBasicCtor();
        testIsIsValidation();
        testDefaultValueForm();
        testLegalAndMinMaxForms();
        testValidationErrors();
        testEqualsHashCode();
        testCrossClassEquals();
        testDescriptorCtor();
        testToStringShape();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: basic 6-arg form (name, desc, OpenType, isRead, isWrite, isIs) ----
    static void testBasicCtor() throws Exception {
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
        // writable-only and read-write variants
        OpenMBeanAttributeInfoSupport w = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, false, true, false);
        check(!w.isReadable() && w.isWritable(), "write-only flags");
        OpenMBeanAttributeInfoSupport rw = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false);
        check(rw.isReadable() && rw.isWritable(), "read-write flags");
        // isIs valid form on BOOLEAN readable
        OpenMBeanAttributeInfoSupport is = new OpenMBeanAttributeInfoSupport("ready", "d", SimpleType.BOOLEAN, true, false, true);
        check(is.isIs() && is.isReadable(), "isIs flags");
        check("java.lang.Boolean".equals(is.getType()), "isIs type Boolean");
    }

    // ---- deepened: is-flag validation (checked at construction) ----
    static void testIsIsValidation() throws Exception {
        checkThrows(IllegalArgumentException.class, "Cannot have an \"is\" getter for a non-readable attribute",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.BOOLEAN, false, true, true); } },
            "isIs without readable");
        checkThrows(IllegalArgumentException.class, "Cannot have an \"is\" getter for a non-boolean attribute",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, false, true); } },
            "isIs on non-boolean openType");
    }

    // ---- deepened: default-value forms ----
    static void testDefaultValueForm() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 3);
        check(a.hasDefaultValue(), "default form has default");
        check(Integer.valueOf(3).equals(a.getDefaultValue()), "default form value");
        check(a.isValue(null), "isValue null true with default");
        check(a.isValue(3), "isValue default");
        check(!a.hasMinValue() && !a.hasMaxValue() && !a.hasLegalValues(), "default form other constraints");
        check(Integer.valueOf(3).equals(a.getDescriptor().getFieldValue("defaultValue")), "descriptor defaultValue");
        // explicit null default allowed, means unspecified
        OpenMBeanAttributeInfoSupport an = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, (Integer) null);
        check(!an.hasDefaultValue(), "null default not registered");
        check(an.getDefaultValue() == null, "null default value");
        check(!an.isValue(null), "isValue null false with null default");
    }

    // ---- deepened: legal-values and min/max forms ----
    static void testLegalAndMinMaxForms() throws Exception {
        OpenMBeanAttributeInfoSupport l = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 2, new Integer[] {1, 2, 3});
        check(l.hasLegalValues(), "legal form has");
        check(l.getLegalValues().size() == 3, "legal size");
        check(l.getLegalValues().contains(1) && l.getLegalValues().contains(3), "legal contents");
        check(l.isValue(1) && !l.isValue(99), "legal isValue bounds");
        try {
            @SuppressWarnings("unchecked")
            Set<Object> raw = (Set<Object>) l.getLegalValues();
            raw.add(4);
            throw new AssertionError("legal set should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            passed++;
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

    // ---- deepened: exact validation error matrix ----
    static void testValidationErrors() throws Exception {
        checkThrows(OpenDataException.class,
            "Argument defaultValue's class [\"java.lang.String\"] does not match the one defined in openType[\"java.lang.Integer\"]",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("p", "d", RAW_INT, true, true, false, "str"); } },
            "default type mismatch");
        checkThrows(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport(null, "d", SimpleType.INTEGER, true, true, false); } },
            "null name");
        checkThrows(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("", "d", SimpleType.INTEGER, true, true, false); } },
            "empty name");
        checkThrows(IllegalArgumentException.class, "Description cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", null, SimpleType.INTEGER, true, true, false); } },
            "null description");
        checkThrows(IllegalArgumentException.class, "OpenType cannot be null",
            new ThrowingCall() { public void run() throws Exception {
                OpenType t = null; new OpenMBeanAttributeInfoSupport("n", "d", t, true, true, false); } },
            "null openType");
        checkThrows(OpenDataException.class, "minValue cannot be greater than defaultValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 0, 1, 10); } },
            "default below min");
        checkThrows(OpenDataException.class, "maxValue cannot be less than defaultValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 99, 1, 10); } },
            "default above max");
        checkThrows(OpenDataException.class, "minValue cannot be greater than maxValue",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, (Integer) null, 20, 10); } },
            "min greater than max");
        checkThrows(OpenDataException.class, "defaultValue is not contained in legalValues",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, 99, new Integer[] {1, 2, 3}); } },
            "default not in legal");
        checkThrows(OpenDataException.class,
            "Element of legalValues [x] is not a valid value for the specified openType [javax.management.openmbean.SimpleType(name=java.lang.Integer)]",
            new ThrowingCall() { public void run() throws Exception {
                new OpenMBeanAttributeInfoSupport("n", "d", RAW_INT, true, true, false, 1, new Object[] {1, "x"}); } },
            "legal element wrong type");
    }

    // ---- deepened: equals/hashCode (flags, default, descriptor participate; desc does not) ----
    static void testEqualsHashCode() throws Exception {
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
        // descriptor participates
        OpenMBeanAttributeInfoSupport c1 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false,
            new ImmutableDescriptor(new String[] {"k"}, new Object[] {"v"}));
        OpenMBeanAttributeInfoSupport c2 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false,
            new ImmutableDescriptor(new String[] {"k"}, new Object[] {"w"}));
        OpenMBeanAttributeInfoSupport c3 = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false,
            new ImmutableDescriptor(new String[] {"k"}, new Object[] {"v"}));
        check(!c1.equals(c2), "descriptor participates");
        check(c1.equals(c3), "same descriptor equal");
        // via interface reference
        OpenMBeanAttributeInfo viaIface = r;
        check(viaIface.isReadable() && !viaIface.isWritable() && !viaIface.isIs(), "flags via interface");
        check(viaIface.equals(r), "equals via interface");
    }

    // ---- deepened: documented cross-class equals asymmetry vs OpenMBeanParameterInfoSupport ----
    static void testCrossClassEquals() throws Exception {
        OpenMBeanAttributeInfoSupport attr = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false);
        OpenMBeanParameterInfoSupport param = new OpenMBeanParameterInfoSupport("n", "d", SimpleType.INTEGER);
        check(!attr.equals(param), "attribute.equals(parameter) is false (attribute demands OpenMBeanAttributeInfo)");
        check(param.equals(attr), "parameter.equals(attribute) is true (only compares name/openType/defaults) - locked asymmetry");
    }

    // ---- deepened: Descriptor constructor form ----
    static void testDescriptorCtor() throws Exception {
        Descriptor d = new ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("n", "d", SimpleType.INTEGER, true, true, false, d);
        check("customValue".equals(a.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(a.isReadable() && a.isWritable(), "descriptor form flags");
        check(a.getOpenType() == SimpleType.INTEGER, "descriptor form openType");
    }

    // ---- deepened: toString shape ----
    static void testToStringShape() throws Exception {
        OpenMBeanAttributeInfoSupport a = new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false, 5, 1, 10);
        String s = a.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanAttributeInfoSupport("), "toString prefix");
        check(s.contains("name=a"), "toString name");
        check(s.contains("default=5"), "toString default");
        check(s.contains("minValue=1") && s.contains("maxValue=10"), "toString min/max");
        check(s.contains("legalValues=null"), "toString legal null");
    }

    // ---- deepened: serialization round-trip ----
    static void testSerialization() throws Exception {
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
        check(ObjectStreamClass.lookup(OpenMBeanAttributeInfoSupport.class).getSerialVersionUID()
            == -4867215622149721849L, "serialVersionUID");
    }
}
