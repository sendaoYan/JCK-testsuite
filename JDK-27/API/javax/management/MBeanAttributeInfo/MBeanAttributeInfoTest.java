/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: white-box structure + black-box behavior.
 *          Deepened: 4-arg Method ctor (type derivation, is-is, DescriptorKey merge,
 *          IntrospectionException matrix), hashCode = name+type only, equals matrix,
 *          toString access words, null-descriptor default, serialization.
 * @run main MBeanAttributeInfoTest
 */

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanAttributeInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getType"), "has getType");
        check(hasMethod(c, "isReadable"), "has isReadable");
        check(hasMethod(c, "isWritable"), "has isWritable");
        check(hasMethod(c, "isIs"), "has isIs");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        check("Count".equals(a.getName()), "getName");
        check("int".equals(a.getType()), "getType");
        check("desc".equals(a.getDescription()), "getDescription");
        check(a.isReadable(), "isReadable");
        check(!a.isWritable(), "isWritable");
        check(!a.isIs(), "isIs");

        // isIs variant
        MBeanAttributeInfo isAttr = new MBeanAttributeInfo("Ready", "boolean", "d", true, false, true);
        check(isAttr.isIs(), "isIs true");
        check(isAttr.isReadable() && !isAttr.isWritable(), "is/isReadable/isWritable combo");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Count", "description=desc");
        MBeanAttributeInfo ad = new MBeanAttributeInfo("Count", "int", "desc", true, true, false, d);
        check(ad.getDescriptor() != null && "Count".equals(ad.getDescriptor().getFieldValue("name")),
                "descriptor ctor");

        // Method-based constructor (introspection)
        MBeanAttributeInfo viaMethod = new MBeanAttributeInfo("V", "desc", getMethod("getV"), getMethod("setV"));
        check("V".equals(viaMethod.getName()), "method ctor name");
        check(viaMethod.isReadable() && viaMethod.isWritable(), "method ctor readable/writable");

        // equals compares all attribute fields
        check(a.equals(new MBeanAttributeInfo("Count", "int", "desc", true, false, false)), "equals identical");
        check(!a.equals(new MBeanAttributeInfo("Count", "int", "desc", true, true, false)), "different writable not equal");
        check(!a.equals(new MBeanAttributeInfo("Count", "long", "desc", true, false, false)), "different type not equal");
        check(!a.equals(new MBeanAttributeInfo("Other", "int", "desc", true, false, false)), "different name not equal");
        check(!a.equals(null), "not equal null");
        check(a.hashCode() == new MBeanAttributeInfo("Count", "int", "desc", true, false, false).hashCode(), "equal hashCode");

        // clone
        MBeanAttributeInfo clone = (MBeanAttributeInfo) a.clone();
        check(clone != a && clone.equals(a), "clone equals original");

        check(a.toString() != null, "toString non-null");
    }

    static Method getMethod(String name) throws Exception {
        for (Method m : Sample.class.getMethods()) if (m.getName().equals(name)) return m;
        throw new NoSuchMethodException(name);
    }
    public static class Sample {
        public int getV() { return 0; }
        public void setV(int v) { }
    }

    // ---- deepened: Method-form constructor ---------------------------------

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Units {
        @DescriptorKey("units") String value();
    }
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SetterTag {
        @DescriptorKey("setterTag") String value();
    }

    public static class Bean {
        @Units("getter-units")
        public int getV() { return 0; }
        @SetterTag("from-setter")
        public void setV(int v) { }
        public void setVLong(long v) { }
        public boolean isReady() { return true; }
        public void setReady(boolean r) { }
        public int getWithArg(int x) { return x; }
        public void doNothing() { }
        public void twoArgs(int a, int b) { }
        @Units("conflict-a")
        public long getL() { return 0L; }
        @Units("conflict-b")
        public void setL(long v) { }
    }

    static void testMethodConstructorForms() throws Exception {
        MBeanAttributeInfo rw = new MBeanAttributeInfo("V", "d",
                Bean.class.getMethod("getV"), Bean.class.getMethod("setV", int.class));
        check("V".equals(rw.getName()), "Method ctor keeps supplied name");
        check("int".equals(rw.getType()), "Method ctor type derived from getter");
        check(rw.isReadable() && rw.isWritable() && !rw.isIs(), "Method ctor readable+writeable");
        check("getter-units".equals(rw.getDescriptor().getFieldValue("units")),
                "Method ctor merges DescriptorKey fields from getter");
        check("from-setter".equals(rw.getDescriptor().getFieldValue("setterTag")),
                "Method ctor merges DescriptorKey fields from setter");

        MBeanAttributeInfo is = new MBeanAttributeInfo("Ready", "d",
                Bean.class.getMethod("isReady"), Bean.class.getMethod("setReady", boolean.class));
        check("boolean".equals(is.getType()), "is-getter derives boolean type");
        check(is.isIs(), "is-getter sets isIs");

        MBeanAttributeInfo wo = new MBeanAttributeInfo("V", "d", null, Bean.class.getMethod("setV", int.class));
        check(!wo.isReadable() && wo.isWritable(), "setter-only is write-only");
        check("int".equals(wo.getType()), "setter-only type from setter parameter");
        check("from-setter".equals(wo.getDescriptor().getFieldValue("setterTag")),
                "setter-only descriptor from setter annotation");

        MBeanAttributeInfo ro = new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"), null);
        check(ro.isReadable() && !ro.isWritable(), "getter-only is read-only");
    }

    static void testMethodConstructorNegatives() throws Exception {
        try {
            new MBeanAttributeInfo("V", "d", null, null);
            check(false, "both null should throw IntrospectionException");
        } catch (IntrospectionException ex) { passed++; }
        try {
            new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getWithArg", int.class), null);
            check(false, "getter with param should throw IntrospectionException");
        } catch (IntrospectionException ex) { passed++; }
        try {
            new MBeanAttributeInfo("V", "d", Bean.class.getMethod("doNothing"), null);
            check(false, "void getter should throw IntrospectionException");
        } catch (IntrospectionException ex) { passed++; }
        try {
            new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"),
                    Bean.class.getMethod("setVLong", long.class));
            check(false, "type mismatch should throw IntrospectionException");
        } catch (IntrospectionException ex) { passed++; }
        try {
            new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"),
                    Bean.class.getMethod("twoArgs", int.class, int.class));
            check(false, "2-param setter should throw IntrospectionException");
        } catch (IntrospectionException ex) { passed++; }
        try {
            new MBeanAttributeInfo("L", "d", Bean.class.getMethod("getL"),
                    Bean.class.getMethod("setL", long.class));
            check(false, "conflicting DescriptorKey values should throw IAE");
        } catch (IllegalArgumentException ex) { passed++; }
    }

    // ---- deepened: hashCode = name+type only -------------------------------

    static void testHashCodeNameTypeOnly() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("N", "int", "descA", true, false, false);
        MBeanAttributeInfo b = new MBeanAttributeInfo("N", "int", "descB", false, true, false);
        check(a.hashCode() == b.hashCode(), "hashCode uses only name and type");
        check(!a.equals(b), "equals still distinguishes description/flags");
    }

    // ---- deepened: equals participation + toString access words -------------

    static void testEqualsAndToString() {
        MBeanAttributeInfo base = new MBeanAttributeInfo("N", "int", "d", true, false, false);
        check(!base.equals(new MBeanAttributeInfo("N", "int", "d", false, false, false)), "readable participates");
        check(!base.equals(new MBeanAttributeInfo("N", "int", "OTHER", true, false, false)), "description participates");
        check(!base.equals(new MBeanAttributeInfo("N", "int", "d", true, false, false,
                new ImmutableDescriptor("k=v"))), "descriptor participates");
        check(base.toString().contains("read-only"), "toString read-only");
        check(new MBeanAttributeInfo("N", "int", "d", false, true, false).toString().contains("write-only"),
                "toString write-only");
        check(new MBeanAttributeInfo("N", "int", "d", true, true, false).toString().contains("read/write"),
                "toString read/write");
        check(new MBeanAttributeInfo("B", "boolean", "d", true, false, true).toString().contains("isIs"),
                "toString marks isIs");
    }

    // ---- deepened: null descriptor default + serialization ------------------

    static void testDescriptorDefaultAndSerialization() throws Exception {
        check(new MBeanAttributeInfo("N", "int", "d", true, false, false).getDescriptor().getFieldNames().length == 0,
                "6-arg ctor yields empty descriptor");
        MBeanAttributeInfo original = new MBeanAttributeInfo("Count", "int", "count", true, true, false,
                new ImmutableDescriptor("units=ms"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanAttributeInfo read = (MBeanAttributeInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check(read.isWritable(), "flags survive serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMethodConstructorForms();
        testMethodConstructorNegatives();
        testHashCodeNameTypeOnly();
        testEqualsAndToString();
        testDescriptorDefaultAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
