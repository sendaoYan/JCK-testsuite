/*
 * @test
 * @summary javax.management.MBeanInfo conformance: full contract coverage.
 *          constructors, accessors, null arrays -> empty arrays, clone deep
 *          independence + equals/hashCode, array getters return defensive
 *          copies (empty arrays shared), equals field participation matrix
 *          (className/description/descriptor/all 4 arrays), default descriptor,
 *          shallow-clone element sharing, serialization round-trip, and the
 *          nested MBeanAttributeInfo isIs validation
 *          (IllegalArgumentException) / MBeanOperationInfo signature defaults.
 * @run main MBeanInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ImmutableDescriptor;

public class MBeanInfoTest {
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

    static MBeanInfo sample() {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, false, false);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0]);
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO);
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        return new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{ai}, new MBeanConstructorInfo[]{ci},
            new MBeanOperationInfo[]{oi}, new MBeanNotificationInfo[]{ni});
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, false, false);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0]);
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO);
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        MBeanInfo info = new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{ai}, new MBeanConstructorInfo[]{ci},
            new MBeanOperationInfo[]{oi}, new MBeanNotificationInfo[]{ni});
        check("com.example.Foo".equals(info.getClassName()), "class name");
        check("desc".equals(info.getDescription()), "description");
        check(info.getAttributes().length == 1 && "int".equals(info.getAttributes()[0].getType()), "attributes");
        check(info.getOperations().length == 1 && "void".equals(info.getOperations()[0].getReturnType()), "operations");
        check(info.getConstructors().length == 1, "constructors");
        check(info.getNotifications().length == 1 && info.getNotifications()[0].getNotifTypes().length == 1, "notifications");
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(info.equals(copy), "clone equals");
    }

    static void testNullArrays() {
        MBeanInfo info = new MBeanInfo("com.example.Foo", "desc", null, null, null, null);
        check(info.getAttributes().length == 0, "null attributes -> empty");
        check(info.getOperations().length == 0, "null operations -> empty");
        check(info.getConstructors().length == 0, "null constructors -> empty");
        check(info.getNotifications().length == 0, "null notifications -> empty");
    }

    static void testCloneIndependence() {
        MBeanInfo info = sample();
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(info.equals(copy) && copy.equals(info), "clone equals symmetric");
        check(info.hashCode() == copy.hashCode(), "clone hashCode");
        // clone is a deep copy: mutating the copy's array does not affect original
        copy.getAttributes()[0] = null;
        check(info.getAttributes()[0] != null, "clone array independent");
    }

    static void testDefensiveArrayGetters() {
        MBeanInfo info = sample();
        MBeanAttributeInfo[] a1 = info.getAttributes();
        a1[0] = null;
        check(info.getAttributes()[0] != null, "getAttributes returns defensive copy");
    }

    static void testAttributeInfoIsIsValidation() {
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "boolean", "d", false, true, true); }
                }, "isIs with non-readable -> IAE");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanAttributeInfo("b", "int", "d", true, false, true); }
                }, "isIs with non-boolean -> IAE");
        // valid: isIs with readable boolean
        MBeanAttributeInfo ok = new MBeanAttributeInfo("b", "boolean", "d", true, false, true);
        check(ok.isIs() && ok.isReadable() && !ok.isWritable(), "isIs valid");
    }

    static void testOperationSignatureDefaults() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d", null, "int", MBeanOperationInfo.INFO);
        check(o.getSignature().length == 0, "null signature -> empty");
        check("int".equals(o.getReturnType()), "return type");
        check(o.getImpact() == MBeanOperationInfo.INFO, "impact");
    }

    // ---- deepened: equals field participation matrix -----------------------

    static void testEqualsFieldMatrix() {
        MBeanInfo base = sample();
        // description participates in equals (spec: same description required)
        MBeanInfo otherDesc = new MBeanInfo("com.example.Foo", "OTHER",
            base.getAttributes(), base.getConstructors(),
            base.getOperations(), base.getNotifications());
        check(!base.equals(otherDesc), "different description not equal");
        // null description is a legal value and participates
        MBeanInfo nd1 = new MBeanInfo("c", null, null, null, null, null);
        MBeanInfo nd2 = new MBeanInfo("c", null, null, null, null, null);
        MBeanInfo nd3 = new MBeanInfo("c", "d", null, null, null, null);
        check(nd1.equals(nd2), "null description equal on both sides");
        check(!nd1.equals(nd3), "null vs non-null description not equal");
        // descriptor participates in equals
        MBeanInfo otherDescr = new MBeanInfo("com.example.Foo", "desc",
            base.getAttributes(), base.getConstructors(),
            base.getOperations(), base.getNotifications(),
            new ImmutableDescriptor("extra=1"));
        check(!base.equals(otherDescr), "different descriptor not equal");
        check(base.equals(sample()), "identical reconstruction equal");
        check(base.hashCode() == sample().hashCode(), "equal objects equal hashCode");
        // each of the four arrays participates individually
        MBeanAttributeInfo otherAttr = new MBeanAttributeInfo("Count", "long", "count", true, false, false);
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{otherAttr}, base.getConstructors(),
            base.getOperations(), base.getNotifications())), "different attributes not equal");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
            base.getAttributes(), new MBeanConstructorInfo[0],
            base.getOperations(), base.getNotifications())), "different constructors not equal");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
            base.getAttributes(), base.getConstructors(),
            new MBeanOperationInfo[0], base.getNotifications())), "different operations not equal");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
            base.getAttributes(), base.getConstructors(),
            base.getOperations(), new MBeanNotificationInfo[0])), "different notifications not equal");
        check(!base.equals(new Object()), "not equal to unrelated type");
    }

    // ---- deepened: array getter sharing vs defensive copy ------------------

    static void testArrayGetterSharingSemantics() {
        // empty internal arrays: both calls return the same shared instance
        // (no defensive copy is made when there is nothing to protect)
        MBeanInfo empty = new MBeanInfo("c", "d", null, null, null, null);
        check(empty.getAttributes() == empty.getAttributes(), "empty getAttributes shared instance");
        check(empty.getOperations() == empty.getOperations(), "empty getOperations shared instance");
        check(empty.getConstructors() == empty.getConstructors(), "empty getConstructors shared instance");
        check(empty.getNotifications() == empty.getNotifications(), "empty getNotifications shared instance");

        // non-empty arrays: every getter returns a fresh defensive copy
        MBeanInfo info = sample();
        MBeanAttributeInfo[] a = info.getAttributes(); a[0] = null;
        check(info.getAttributes()[0] != null, "getAttributes defensive copy (element)");
        MBeanOperationInfo[] o = info.getOperations(); o[0] = null;
        check(info.getOperations()[0] != null, "getOperations defensive copy (element)");
        MBeanConstructorInfo[] c = info.getConstructors(); c[0] = null;
        check(info.getConstructors()[0] != null, "getConstructors defensive copy (element)");
        MBeanNotificationInfo[] n = info.getNotifications(); n[0] = null;
        check(info.getNotifications()[0] != null, "getNotifications defensive copy (element)");
        // defensive copy also means a fresh array each call (length cannot be changed)
        check(info.getAttributes() != info.getAttributes(), "non-empty getAttributes fresh array per call");
    }

    // ---- deepened: clone is shallow (elements shared with original) --------

    static void testCloneSharesElements() {
        MBeanInfo info = sample();
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(copy != info, "clone distinct object");
        check(copy.getAttributes()[0] == info.getAttributes()[0],
                "clone shares element references (shallow clone)");
    }

    // ---- deepened: default descriptor + provided descriptor ----------------

    static void testDescriptorDefaults() {
        MBeanInfo plain = sample();
        check(plain.getDescriptor() != null, "6-arg ctor yields non-null descriptor");
        check(plain.getDescriptor().getFieldNames().length == 0, "default descriptor empty");
        check(plain.getDescriptor() instanceof ImmutableDescriptor, "default descriptor is ImmutableDescriptor");
        ImmutableDescriptor d = new ImmutableDescriptor("visibility=1");
        MBeanInfo with = new MBeanInfo("c", "d", null, null, null, null, d);
        check("1".equals(with.getDescriptor().getFieldValue("visibility")),
                "7-arg ctor preserves descriptor field");
        // null descriptor in 7-arg ctor is equivalent to an empty descriptor
        MBeanInfo nullD = new MBeanInfo("c", "d", null, null, null, null, null);
        check(nullD.getDescriptor().getFieldNames().length == 0, "null descriptor -> empty descriptor");
    }

    // ---- deepened: serialization round-trip -------------------------------

    static void testSerialization() throws Exception {
        MBeanInfo original = new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{new MBeanAttributeInfo("Count", "int", "count", true, false, false)},
            new MBeanConstructorInfo[]{new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0])},
            new MBeanOperationInfo[]{new MBeanOperationInfo("op", "desc",
                new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "pd")},
                "void", MBeanOperationInfo.INFO)},
            new MBeanNotificationInfo[]{new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc")},
            new ImmutableDescriptor("k=v"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanInfo read = (MBeanInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("com.example.Foo".equals(read.getClassName()), "className survives serialization");
        check("desc".equals(read.getDescription()), "description survives serialization");
        check(read.getAttributes().length == 1 && "Count".equals(read.getAttributes()[0].getName()),
                "attributes survive serialization");
        check(read.getOperations()[0].getSignature()[0].getName().equals("p"),
                "nested signature survives serialization");
        check("v".equals(read.getDescriptor().getFieldValue("k")), "descriptor survives serialization");
        // null-arrays MBeanInfo (null description) round-trips with empty arrays
        MBeanInfo nulls = new MBeanInfo("c", null, null, null, null, null);
        bos.reset();
        new ObjectOutputStream(bos).writeObject(nulls);
        MBeanInfo read2 = (MBeanInfo) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(read2.getDescription() == null, "null description preserved through serialization");
        check(read2.getAttributes().length == 0, "null attributes read back as empty");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullArrays();
        testCloneIndependence();
        testDefensiveArrayGetters();
        testAttributeInfoIsIsValidation();
        testOperationSignatureDefaults();
        testEqualsFieldMatrix();
        testArrayGetterSharingSemantics();
        testCloneSharesElements();
        testDescriptorDefaults();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
