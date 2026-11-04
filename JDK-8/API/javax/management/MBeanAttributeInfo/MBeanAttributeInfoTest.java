/*
 * @test
 * @summary javax.management.MBeanAttributeInfo conformance: full contract
 *          coverage. type/readable/writable/is accessors, isIs validation
 *          (IllegalArgumentException), 4-arg Method constructor (type
 *          derivation, is-is detection, DescriptorKey-driven descriptor,
 *          IntrospectionException matrix), hashCode = name+type only,
 *          equals participation matrix, toString access words, null-descriptor
 *          defaulting, clone/equals/hashCode, descriptor ctor, serialization.
 * @run main MBeanAttributeInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;

public class MBeanAttributeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "count", true, true, false);
        check("Count".equals(a.getName()), "name");
        check("int".equals(a.getType()), "type");
        check(a.isReadable() && a.isWritable() && !a.isIs(), "flags");
        MBeanAttributeInfo c = (MBeanAttributeInfo) a.clone();
        check(a.equals(c), "clone equals");
    }

    static void testIsIsValidation() {
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "boolean", "d", false, true, true); }
                }, "isIs non-readable");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "int", "d", true, false, true); }
                }, "isIs non-boolean");
        MBeanAttributeInfo ok = new MBeanAttributeInfo("b", "java.lang.Boolean", "d", true, true, true);
        check(ok.isIs() && ok.isReadable() && ok.isWritable(), "isIs Boolean valid");
    }

    static void testEqualsHashCodeDescriptor() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        MBeanAttributeInfo b = new MBeanAttributeInfo("Count", "int", "desc", true, false, false);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(!a.equals(new MBeanAttributeInfo("Count", "long", "desc", true, false, false)), "different type");
        Descriptor d = new ImmutableDescriptor(new String[]{"units"}, new Object[]{"ms"});
        MBeanAttributeInfo withD = new MBeanAttributeInfo("Count", "int", "desc", true, false, false, d);
        check("ms".equals(withD.getDescriptor().getFieldValue("units")), "descriptor ctor");
        check(withD.toString() != null && withD.toString().length() > 0, "toString");
    }

    // ---- deepened: Method-form constructor ---------------------------------

    /** Annotation whose elements are mapped into descriptors via @DescriptorKey. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Units {
        @javax.management.DescriptorKey("units")
        String value();
    }

    /** A second annotation contributing a different descriptor field. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface SetterTag {
        @javax.management.DescriptorKey("setterTag")
        String value();
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
        // getter + setter: name comes from the ctor argument, type derived from methods
        MBeanAttributeInfo rw = new MBeanAttributeInfo("V", "d",
                Bean.class.getMethod("getV"), Bean.class.getMethod("setV", int.class));
        check("V".equals(rw.getName()), "Method ctor keeps supplied name");
        check("int".equals(rw.getType()), "Method ctor type derived from getter");
        check(rw.isReadable() && rw.isWritable() && !rw.isIs(), "Method ctor readable+writeable");
        check("getter-units".equals(rw.getDescriptor().getFieldValue("units")),
                "Method ctor merges DescriptorKey fields from getter");
        check("from-setter".equals(rw.getDescriptor().getFieldValue("setterTag")),
                "Method ctor merges DescriptorKey fields from setter");

        // is-form getter: type boolean + isIs true
        MBeanAttributeInfo is = new MBeanAttributeInfo("Ready", "d",
                Bean.class.getMethod("isReady"), Bean.class.getMethod("setReady", boolean.class));
        check("boolean".equals(is.getType()), "is-getter derives boolean type");
        check(is.isIs(), "is-getter sets isIs");

        // setter only: write-only, type taken from setter parameter
        MBeanAttributeInfo wo = new MBeanAttributeInfo("V", "d", null, Bean.class.getMethod("setV", int.class));
        check(!wo.isReadable() && wo.isWritable(), "setter-only is write-only");
        check("int".equals(wo.getType()), "setter-only type from setter parameter");
        check("from-setter".equals(wo.getDescriptor().getFieldValue("setterTag")),
                "setter-only descriptor from setter annotation");

        // getter only: read-only
        MBeanAttributeInfo ro = new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"), null);
        check(ro.isReadable() && !ro.isWritable(), "getter-only is read-only");

        // getter returns int but setter takes long -> type mismatch
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"),
                                Bean.class.getMethod("setVLong", long.class));
                    }
                }, "type mismatch between getter and setter");
    }

    static void testMethodConstructorIntrospectionExceptions() throws Exception {
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("V", "d", null, null);
                    }
                }, "getter and setter both null -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getWithArg", int.class), null);
                    }
                }, "getter with parameter -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("V", "d", Bean.class.getMethod("doNothing"), null);
                    }
                }, "void getter -> IntrospectionException");
    }

    static void testMethodConstructorSetterArity() throws Exception {
        // a setter taking two arguments is inconsistent -> IntrospectionException
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("V", "d", Bean.class.getMethod("getV"),
                                Bean.class.getMethod("twoArgs", int.class, int.class));
                    }
                }, "setter with 2 params -> IntrospectionException");
        // DescriptorKey consistency rule: getter and setter annotating the SAME
        // descriptor field with DIFFERENT values is rejected with IAE
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new MBeanAttributeInfo("L", "d", Bean.class.getMethod("getL"),
                                Bean.class.getMethod("setL", long.class));
                    }
                }, "conflicting DescriptorKey values on getter/setter -> IAE");
    }

    // ---- deepened: hashCode covers only name+type ---------------------------

    static void testHashCodeNameTypeOnly() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("N", "int", "descA", true, false, false);
        MBeanAttributeInfo b = new MBeanAttributeInfo("N", "int", "descB", false, true, false);
        check(a.hashCode() == b.hashCode(), "hashCode uses only name and type");
        check(!a.equals(b), "but equals still distinguishes description/flags");
    }

    // ---- deepened: equals participation matrix ------------------------------

    static void testEqualsParticipationMatrix() {
        MBeanAttributeInfo base = new MBeanAttributeInfo("N", "int", "d", true, false, false);
        check(!base.equals(new MBeanAttributeInfo("N", "int", "d", false, false, false)), "readable participates");
        check(!base.equals(new MBeanAttributeInfo("N", "int", "d", true, true, false)), "writable participates");
        check(!base.equals(new MBeanAttributeInfo("N", "int", "OTHER", true, false, false)), "description participates");
        check(!base.equals(new MBeanAttributeInfo("N", "int", "d", true, false, false,
                new ImmutableDescriptor("k=v"))), "descriptor participates");
        check(!base.equals(null) && !base.equals("N"), "equals null/foreign type false");
    }

    // ---- deepened: toString access words ------------------------------------

    static void testToStringAccess() {
        String ro = new MBeanAttributeInfo("N", "int", "d", true, false, false).toString();
        check(ro.contains("read-only"), "toString read-only");
        String wo = new MBeanAttributeInfo("N", "int", "d", false, true, false).toString();
        check(wo.contains("write-only"), "toString write-only");
        String rw = new MBeanAttributeInfo("N", "int", "d", true, true, false).toString();
        check(rw.contains("read/write"), "toString read/write");
        String is = new MBeanAttributeInfo("B", "boolean", "d", true, false, true).toString();
        check(is.contains("isIs"), "toString marks isIs");
        check(rw.contains("name=N") && rw.contains("type=int") && rw.contains("description=d"),
                "toString carries name/type/description");
    }

    // ---- deepened: null descriptor defaults to empty ------------------------

    static void testNullDescriptorDefault() {
        MBeanAttributeInfo a = new MBeanAttributeInfo("N", "int", "d", true, false, false);
        check(a.getDescriptor() != null && a.getDescriptor().getFieldNames().length == 0,
                "6-arg ctor yields empty descriptor");
        check(a.getDescriptor() instanceof ImmutableDescriptor, "descriptor type ImmutableDescriptor");
    }

    // ---- deepened: serialization round-trip ---------------------------------

    static void testSerialization() throws Exception {
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
        testBasic();
        testIsIsValidation();
        testEqualsHashCodeDescriptor();
        testMethodConstructorForms();
        testMethodConstructorIntrospectionExceptions();
        testMethodConstructorSetterArity();
        testHashCodeNameTypeOnly();
        testEqualsParticipationMatrix();
        testToStringAccess();
        testNullDescriptorDefault();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
