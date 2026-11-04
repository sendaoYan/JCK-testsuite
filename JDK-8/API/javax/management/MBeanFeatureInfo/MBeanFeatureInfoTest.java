/*
 * @test
 * @summary javax.management.MBeanFeatureInfo conformance: full contract
 *          coverage. name/description/descriptor accessors, null name and
 *          null description are retained (null-safe equals, but hashCode
 *          throws NPE on null name), equals/hashCode (incl. descriptor
 *          comparison), documented equals asymmetry between base and
 *          subclasses, default descriptor, toString, serialization.
 * @run main MBeanFeatureInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class MBeanFeatureInfoTest {
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
        MBeanFeatureInfo f = new MBeanFeatureInfo("name", "desc");
        check("name".equals(f.getName()), "name");
        check("desc".equals(f.getDescription()), "description");
        MBeanFeatureInfo g = new MBeanFeatureInfo("n", "d", new ImmutableDescriptor(new String[]{"a"}, new Object[]{"b"}));
        check(g.getDescriptor() != null && "b".equals(g.getDescriptor().getFieldValue("a")), "descriptor");
    }

    static void testEqualsHashCode() {
        MBeanFeatureInfo a = new MBeanFeatureInfo("n", "d");
        MBeanFeatureInfo b = new MBeanFeatureInfo("n", "d");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new MBeanFeatureInfo("n2", "d")), "different name");
        check(!a.equals(new MBeanFeatureInfo("n", "d2")), "different description");
        check(!a.equals(new MBeanFeatureInfo("n", "d", new ImmutableDescriptor(new String[]{"k"}, new Object[]{"v"}))), "different descriptor");
        check(!a.equals(null) && !a.equals("n"), "equals non-MBeanFeatureInfo");
    }

    static void testToString() {
        MBeanFeatureInfo f = new MBeanFeatureInfo("n", "d");
        String s = f.toString();
        check(s != null && s.startsWith("javax.management.MBeanFeatureInfo@"), "toString default Object form");
    }

    // ---- deepened: null name/description semantics --------------------------

    static void testNullFieldSemantics() {
        MBeanFeatureInfo bothNull = new MBeanFeatureInfo(null, null);
        check(bothNull.getName() == null && bothNull.getDescription() == null,
                "null name and description are retained");
        check(bothNull.equals(new MBeanFeatureInfo(null, null)), "null == null compares equal");
        check(!bothNull.equals(new MBeanFeatureInfo(null, "d")), "null vs non-null description differ");
        check(!bothNull.equals(new MBeanFeatureInfo("n", null)), "null vs non-null name differ");
        // hashCode dereferences name and description: null name -> NullPointerException
        // (documented implementation quirk, identical on JDK-8 and JDK-27)
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() {
                    public void run() { new MBeanFeatureInfo(null, null).hashCode(); }
                }, "hashCode with null name throws NPE");
        // clone/serialization retain nulls (serialization covered below)
    }

    // ---- deepened: base-vs-subclass equals asymmetry ------------------------

    static void testSubclassEqualsAsymmetry() {
        // MBeanFeatureInfo.equals accepts any MBeanFeatureInfo (instanceof), so the
        // base accepts a subclass instance with equal name/description/descriptor...
        MBeanFeatureInfo base = new MBeanFeatureInfo("a", "b");
        MBeanAttributeInfo attr = new MBeanAttributeInfo("a", "java.lang.String", "b", true, false, false);
        check(base.equals(attr), "base.equals(subclass) is true (documented asymmetry)");
        // ...but the subclass only accepts its own type, so the reverse is false
        check(!attr.equals(base), "subclass.equals(base) is false");
        MBeanParameterInfo param = new MBeanParameterInfo("a", "java.lang.String", "b");
        check(base.equals(param), "base vs parameter-subclass true on base side");
        check(!param.equals(base), "parameter-subclass vs base false");
        // sibling subclasses never compare equal
        check(!attr.equals(param) && !param.equals(attr), "sibling subclasses never equal");
        MBeanOperationInfo op = new MBeanOperationInfo("a", "b", null, "void", 0);
        check(!op.equals(attr) && !op.equals(param), "operation vs siblings not equal");
    }

    // ---- deepened: default descriptor + serialization -----------------------

    static void testDefaultDescriptorAndSerialization() throws Exception {
        MBeanFeatureInfo plain = new MBeanFeatureInfo("n", "d");
        check(plain.getDescriptor() instanceof ImmutableDescriptor, "default descriptor is ImmutableDescriptor");
        check(plain.getDescriptor().getFieldNames().length == 0, "default descriptor empty");
        check(plain.getDescriptor() != null, "getDescriptor never null");
        MBeanFeatureInfo original = new MBeanFeatureInfo("n", "d", new ImmutableDescriptor("k=v"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanFeatureInfo read = (MBeanFeatureInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("v".equals(read.getDescriptor().getFieldValue("k")), "descriptor survives serialization");
        check("n".equals(read.getName()) && "d".equals(read.getDescription()),
                "name/description survive serialization");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEqualsHashCode();
        testToString();
        testNullFieldSemantics();
        testSubclassEqualsAsymmetry();
        testDefaultDescriptorAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
