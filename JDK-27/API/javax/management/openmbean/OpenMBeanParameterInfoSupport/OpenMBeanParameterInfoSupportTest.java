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
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
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
        // ---- deepened: batch g ----
        testBasicCtorG();
        testDefaultValueCtorG();
        testLegalValuesCtorG();
        testMinMaxCtorG();
        testValidationErrorsG();
        testDescriptorCtorG();
        testEqualsHashCodeG();
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
    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    @SuppressWarnings("rawtypes")
    static final OpenType RAW_INT = SimpleType.INTEGER;

    static void testBasicCtorG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "desc", SimpleType.INTEGER);
        check("p".equals(p.getName()), "basic name");
        check("desc".equals(p.getDescription()), "basic desc");
        check(p.getOpenType() == SimpleType.INTEGER, "basic openType is same SimpleType singleton");
        check(!p.hasDefaultValue() && !p.hasLegalValues() && !p.hasMinValue() && !p.hasMaxValue(), "basic no constraints");
        check(p.getDefaultValue() == null, "basic default null");
        check(p.getLegalValues() == null, "basic legal null");
        check(p.getMinValue() == null, "basic min null");
        check(p.getMaxValue() == null, "basic max null");
        check(p.isValue(5), "isValue Integer autoboxed");
        check(p.isValue(Integer.valueOf(7)), "isValue Integer valueOf");
        check(!p.isValue("x"), "isValue reject String");
        check(!p.isValue(null), "isValue reject null without default");
        check(!p.isValue(5L), "isValue reject Long");
        check("java.lang.Integer".equals(p.getType()), "getType from openType class name");
        check(p.getDescriptor() != null, "descriptor non-null");
    }

    static void testDefaultValueCtorG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 3);
        check(p.hasDefaultValue(), "has default");
        check(Integer.valueOf(3).equals(p.getDefaultValue()), "default value");
        check(p.isValue(3), "isValue default");
        check(p.isValue(null), "isValue null true when default set");
        check(!p.hasMinValue() && !p.hasMaxValue() && !p.hasLegalValues(), "no min/max/legal with default");
        check(Integer.valueOf(3).equals(p.getDescriptor().getFieldValue("defaultValue")), "descriptor defaultValue field");
        OpenMBeanParameterInfoSupport pn = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, (Integer) null);
        check(!pn.hasDefaultValue(), "explicit null default not registered");
        check(pn.getDefaultValue() == null, "explicit null default value null");
        check(!pn.isValue(null), "isValue null false with null default");
        check(pn.getDescriptor().getFieldValue("defaultValue") == null, "no defaultValue descriptor field");
        @SuppressWarnings("rawtypes") OpenType voidType = SimpleType.VOID;
        new OpenMBeanParameterInfoSupport("p", "d", voidType, (Object) null);
        check(true, "SimpleType.VOID accepts null default without error");
    }

    static void testLegalValuesCtorG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 2, new Integer[] {1, 2, 3});
        check(p.hasLegalValues(), "has legal values");
        check(p.hasDefaultValue(), "legal form default");
        check(Integer.valueOf(2).equals(p.getDefaultValue()), "legal form default value");
        check(!p.hasMinValue() && !p.hasMaxValue(), "no min/max in legal form");
        Set<?> legal = p.getLegalValues();
        check(legal.size() == 3, "legal size");
        check(legal.contains(1) && legal.contains(2) && legal.contains(3), "legal contents");
        check(!legal.contains(99), "legal excludes 99");
        check(p.isValue(1) && p.isValue(3), "isValue inside legal");
        check(!p.isValue(99), "isValue outside legal");
        check(!p.isValue("x"), "isValue wrong type outside legal");
        try {
            @SuppressWarnings("unchecked")
            Set<Object> raw = (Set<Object>) p.getLegalValues();
            raw.add(4);
            throw new AssertionError("legal set should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "legal set unmodifiable");
        }
        check(p.getLegalValues() == p.getLegalValues(), "getLegalValues stable reference");
        Object descLegal = p.getDescriptor().getFieldValue("legalValues");
        check(descLegal instanceof Set, "descriptor legalValues is Set");
        check(((Set<?>) descLegal).size() == 3, "descriptor legalValues size");
        OpenMBeanParameterInfoSupport pe = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, (Integer) null, new Integer[0]);
        check(pe.hasLegalValues(), "empty array still registers legal set");
        check(pe.getLegalValues().size() == 0, "empty legal set size 0");
        check(!pe.hasDefaultValue(), "empty legal no default");
        check(!pe.isValue(1), "empty legal rejects everything");
    }

    static void testMinMaxCtorG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10);
        check(p.hasMinValue() && Integer.valueOf(1).equals(p.getMinValue()), "min value");
        check(p.hasMaxValue() && Integer.valueOf(10).equals(p.getMaxValue()), "max value");
        check(p.hasDefaultValue() && Integer.valueOf(5).equals(p.getDefaultValue()), "min/max default");
        check(!p.hasLegalValues(), "no legal in min/max form");
        check(p.isValue(1), "boundary min isValue");
        check(p.isValue(10), "boundary max isValue");
        check(!p.isValue(0), "below min rejected");
        check(!p.isValue(11), "above max rejected");
        check(p.isValue(5), "default isValue");
        OpenMBeanParameterInfoSupport ps = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING, "m", "a", "z");
        check("a".equals(ps.getMinValue()) && "z".equals(ps.getMaxValue()), "string min/max");
        check(ps.isValue("b"), "string in range");
        check(!ps.isValue("zzz"), "string above max");
        OpenMBeanParameterInfoSupport pd = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 5, 5);
        check(pd.isValue(5) && !pd.isValue(4) && !pd.isValue(6), "degenerate range");
        Descriptor d = p.getDescriptor();
        check(Integer.valueOf(1).equals(d.getFieldValue("minValue")), "descriptor minValue");
        check(Integer.valueOf(10).equals(d.getFieldValue("maxValue")), "descriptor maxValue");
        check(Integer.valueOf(5).equals(d.getFieldValue("defaultValue")), "descriptor defaultValue");
        check(SimpleType.INTEGER.equals(d.getFieldValue("openType")), "descriptor openType");
        check(d.getFieldValue("legalValues") == null, "no legalValues field in min/max form");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (String f : d.getFieldNames()) names.add(f);
        check(names.contains("defaultValue") && names.contains("minValue") && names.contains("maxValue") && names.contains("openType"),
            "descriptor field names");
    }

    static void testValidationErrorsG() throws Exception {
        checkThrowsMsg(OpenDataException.class,
            "Argument defaultValue's class [\"java.lang.String\"] does not match the one defined in openType[\"java.lang.Integer\"]",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", RAW_INT, "str"); } },
            "default value type mismatch");
        checkThrowsMsg(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport(null, "d", SimpleType.INTEGER); } },
            "null name");
        checkThrowsMsg(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("", "d", SimpleType.INTEGER); } },
            "empty name");
        checkThrowsMsg(IllegalArgumentException.class, "Name cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("   ", "d", SimpleType.INTEGER); } },
            "blank name");
        checkThrowsMsg(IllegalArgumentException.class, "Description cannot be null or empty",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", null, SimpleType.INTEGER); } },
            "null description");
        checkThrowsMsg(IllegalArgumentException.class, "OpenType cannot be null",
            new ThrowingCall() { public void run() throws Exception { OpenType t = null; new OpenMBeanParameterInfoSupport("p", "d", t); } },
            "null openType");
        checkThrowsMsg(OpenDataException.class, "minValue cannot be greater than defaultValue",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 0, 1, 10); } },
            "default below min");
        checkThrowsMsg(OpenDataException.class, "maxValue cannot be less than defaultValue",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 99, 1, 10); } },
            "default above max");
        checkThrowsMsg(OpenDataException.class, "minValue cannot be greater than maxValue",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, (Integer) null, 20, 10); } },
            "min greater than max with null default");
        checkThrowsMsg(OpenDataException.class, "defaultValue is not contained in legalValues",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 99, new Integer[] {1, 2, 3}); } },
            "default not in legal");
        checkThrowsMsg(OpenDataException.class, "defaultValue is not contained in legalValues",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 2, new Integer[0]); } },
            "default not in empty legal");
        checkThrowsMsg(OpenDataException.class,
            "Element of legalValues [x] is not a valid value for the specified openType [javax.management.openmbean.SimpleType(name=java.lang.Integer)]",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", RAW_INT, 1, new Object[] {1, "x", 3}); } },
            "legal element wrong type");
        checkThrowsMsg(OpenDataException.class,
            "Element of legalValues [null] is not a valid value for the specified openType [javax.management.openmbean.SimpleType(name=java.lang.Integer)]",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1, new Integer[] {1, null, 3}); } },
            "legal element null");
    }

    static void testDescriptorCtorG() throws Exception {
        Descriptor d = new ImmutableDescriptor(new String[] {"minValue", "maxValue"}, new Object[] {1, 10});
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, d);
        check(p.hasMinValue() && Integer.valueOf(1).equals(p.getMinValue()), "descriptor ctor min");
        check(p.hasMaxValue() && Integer.valueOf(10).equals(p.getMaxValue()), "descriptor ctor max");
        check(!p.hasDefaultValue(), "descriptor ctor no default");
        check(p.getOpenType() == SimpleType.INTEGER, "descriptor ctor openType");
        check(!p.hasLegalValues(), "descriptor ctor no legal");
        Descriptor d2 = new ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanParameterInfoSupport p2 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, d2);
        check("customValue".equals(p2.getDescriptor().getFieldValue("customKey")), "extra descriptor field preserved");
        Descriptor bad = new ImmutableDescriptor(new String[] {"legalValues", "minValue"},
            new Object[] {new java.util.HashSet<Object>(java.util.Arrays.asList(1, 2)), 1});
        checkThrowsMsg(IllegalArgumentException.class, "cannot have both legalValue and minValue or maxValue",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, bad); } },
            "descriptor legal plus min");
        Descriptor bad2 = new ImmutableDescriptor(new String[] {"defaultValue"}, new Object[] {"str"});
        checkThrowsMsg(IllegalArgumentException.class, "Cannot convert descriptor field defaultValue  to java.lang.Integer",
            new ThrowingCall() { public void run() throws Exception { new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, bad2); } },
            "descriptor unconvertible default");
    }

    static void testEqualsHashCodeG() throws Exception {
        OpenMBeanParameterInfoSupport a = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER);
        OpenMBeanParameterInfoSupport b = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER);
        check(a.equals(b), "equal same shape");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(new OpenMBeanParameterInfoSupport("q", "d", SimpleType.INTEGER)), "different name");
        check(!a.equals(new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING)), "different openType");
        check(a.equals(new OpenMBeanParameterInfoSupport("p", "OTHER", SimpleType.INTEGER)), "description not in equals");
        OpenMBeanParameterInfoSupport d1 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1);
        OpenMBeanParameterInfoSupport d2 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 2);
        check(!d1.equals(d2), "different default not equal");
        check(!d2.equals(d1), "different default symmetric");
        check(d1.equals(new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1)), "same default equal");
        check(!a.equals(d1), "no default vs default not equal");
        OpenMBeanParameterInfoSupport m1 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10);
        OpenMBeanParameterInfoSupport m2 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 2, 10);
        check(!m1.equals(m2), "different min not equal");
        check(m1.equals(new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10)), "same min/max equal");
        check(m1.hashCode() == new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10).hashCode(), "min/max hashCode");
        OpenMBeanParameterInfoSupport l1 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1, new Integer[] {1, 2});
        OpenMBeanParameterInfoSupport l2 = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1, new Integer[] {1, 3});
        check(!l1.equals(l2), "different legal not equal");
        check(l1.equals(new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 1, new Integer[] {2, 1})), "legal set order irrelevant");
        OpenMBeanParameterInfo viaIface = a;
        check(viaIface.equals(b), "equals via interface reference");
    }

    static void testToStringShapeG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10);
        String s = p.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanParameterInfoSupport("), "toString prefix");
        check(s.contains("name=p"), "toString contains name");
        check(s.contains("openType=javax.management.openmbean.SimpleType(name=java.lang.Integer)"), "toString contains openType");
        check(s.contains("default=5"), "toString contains default");
        check(s.contains("minValue=1"), "toString contains minValue");
        check(s.contains("maxValue=10"), "toString contains maxValue");
        check(s.contains("legalValues=null"), "toString contains legalValues=null");
        OpenMBeanParameterInfoSupport plain = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER);
        check(plain.toString().contains("default=null"), "toString default null");
    }

    static void testSerializationG() throws Exception {
        OpenMBeanParameterInfoSupport p = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 5, 1, 10);
        Object o = roundTrip(p);
        check(o instanceof OpenMBeanParameterInfoSupport, "round-trip class");
        OpenMBeanParameterInfoSupport q = (OpenMBeanParameterInfoSupport) o;
        check(p.equals(q), "round-trip equals");
        check(q.hashCode() == p.hashCode(), "round-trip hashCode");
        check(q.getOpenType() == SimpleType.INTEGER, "round-trip openType singleton");
        check(Integer.valueOf(1).equals(q.getMinValue()), "round-trip min");
        check(Integer.valueOf(10).equals(q.getMaxValue()), "round-trip max");
        check(q.hasMinValue() && q.hasMaxValue() && q.hasDefaultValue(), "round-trip has flags");
        OpenMBeanParameterInfoSupport l = new OpenMBeanParameterInfoSupport("p", "d", SimpleType.INTEGER, 2, new Integer[] {1, 2, 3});
        OpenMBeanParameterInfoSupport lq = (OpenMBeanParameterInfoSupport) roundTrip(l);
        check(l.equals(lq), "legal round-trip equals");
        check(lq.getLegalValues().size() == 3, "legal round-trip contents");
        check(lq.isValue(3) && !lq.isValue(9), "legal round-trip isValue");
        try {
            @SuppressWarnings("unchecked")
            Set<Object> raw = (Set<Object>) lq.getLegalValues();
            raw.add(4);
            throw new AssertionError("round-trip legal set unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "round-trip legal set unmodifiable");
        }
        check(java.io.ObjectStreamClass.lookup(OpenMBeanParameterInfoSupport.class).getSerialVersionUID()
            == -7235016873758443122L, "serialVersionUID");
    }
}

