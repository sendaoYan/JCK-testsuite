/*
 * @test
 * @summary javax.management.MBeanConstructorInfo conformance: full contract
 *          coverage. signature, null signature -> empty, defensive getter
 *          (empty shared), (String, Constructor) form with name == declaring
 *          class binary name, equals participation matrix (name/description/
 *          signature/descriptor), hashCode = name ^ signature hash, descriptor
 *          ctor round-trip + null -> empty, clone/equals, serialization.
 * @run main MBeanConstructorInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;

public class MBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[]{p});
        check("ctor".equals(c.getName()), "name");
        check(c.getSignature().length == 1 && "x".equals(c.getSignature()[0].getName()), "signature");
    }

    static void testNullSignatureAndDefensiveGetter() {
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc", null);
        check(c.getSignature().length == 0, "null signature -> empty");

        MBeanConstructorInfo withParam = new MBeanConstructorInfo("ctor", "desc",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")});
        withParam.getSignature()[0] = null;
        check(withParam.getSignature()[0] != null, "defensive copy");
    }

    static void testCloneEquals() {
        MBeanConstructorInfo c = new MBeanConstructorInfo("ctor", "desc",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")});
        MBeanConstructorInfo copy = (MBeanConstructorInfo) c.clone();
        check(c.equals(copy) && c.hashCode() == copy.hashCode(), "clone equals/hashCode");
    }

    // ---- deepened: (String, Constructor) form -------------------------------

    public static class CtorSample {
        public CtorSample(int x, String s) { }
    }

    static void testConstructorReflectionForm() throws Exception {
        Constructor<CtorSample> ctor = CtorSample.class.getConstructor(int.class, String.class);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor desc", ctor);
        check(CtorSample.class.getName().equals(ci.getName()),
                "Constructor form name is declaring class binary name");
        check("ctor desc".equals(ci.getDescription()), "Constructor form description from first arg");
        check(ci.getSignature().length == 2, "Constructor form signature length");
        check("int".equals(ci.getSignature()[0].getType()) && "java.lang.String".equals(ci.getSignature()[1].getType()),
                "Constructor form param types");
        check("p1".equals(ci.getSignature()[0].getName()), "Constructor form param named p1");
        check(ci.getDescriptor().getFieldNames().length == 0, "Constructor form default descriptor empty");
    }

    // ---- deepened: empty signature shared + descriptor ctor -----------------

    static void testEmptySignatureAndDescriptorCtor() {
        MBeanConstructorInfo empty = new MBeanConstructorInfo("n", "d", new MBeanParameterInfo[0]);
        check(empty.getSignature() == empty.getSignature(), "empty signature returns shared instance");
        MBeanConstructorInfo withD = new MBeanConstructorInfo("n", "d", null,
                new ImmutableDescriptor("x=9"));
        check("9".equals(withD.getDescriptor().getFieldValue("x")), "descriptor ctor round-trip");
        check(new MBeanConstructorInfo("n", "d", null, null).getDescriptor().getFieldNames().length == 0,
                "null descriptor -> empty");
        check(new MBeanConstructorInfo("n", "d", null).getDescriptor().getFieldNames().length == 0,
                "3-arg ctor descriptor empty");
    }

    // ---- deepened: equals participation + hashCode shape --------------------

    static void testEqualsAndHashCode() {
        MBeanParameterInfo[] sig = { new MBeanParameterInfo("p", "int", "d") };
        MBeanConstructorInfo base = new MBeanConstructorInfo("n", "d", sig);
        check(!base.equals(new MBeanConstructorInfo("OTHER", "d", sig)), "name participates in equals");
        check(!base.equals(new MBeanConstructorInfo("n", "OTHER", sig)), "description participates in equals");
        check(!base.equals(new MBeanConstructorInfo("n", "d", new MBeanParameterInfo[0])),
                "signature participates in equals");
        check(!base.equals(new MBeanConstructorInfo("n", "d", sig, new ImmutableDescriptor("k=v"))),
                "descriptor participates in equals");
        check(!base.equals(null) && !base.equals("n"), "equals null/foreign false");
        // hashCode = name ^ Arrays.hashCode(signature): same name, different signature -> different hash
        int h1 = new MBeanConstructorInfo("n", "d", sig).hashCode();
        int h2 = new MBeanConstructorInfo("n", "d",
                new MBeanParameterInfo[]{new MBeanParameterInfo("q", "long", "d")}).hashCode();
        check(h1 != h2, "hashCode sensitive to signature content");
        check(base.hashCode() == new MBeanConstructorInfo("n", "d", sig).hashCode(),
                "equal objects equal hashCode");
    }

    // ---- deepened: serialization round-trip ---------------------------------

    static void testSerialization() throws Exception {
        MBeanConstructorInfo original = new MBeanConstructorInfo("n", "d",
                new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "pd")},
                new ImmutableDescriptor("k=v"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanConstructorInfo read = (MBeanConstructorInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("v".equals(read.getDescriptor().getFieldValue("k")), "descriptor survives serialization");
        check(read.getSignature()[0].getName().equals("p"), "signature survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullSignatureAndDefensiveGetter();
        testCloneEquals();
        testConstructorReflectionForm();
        testEmptySignatureAndDescriptorCtor();
        testEqualsAndHashCode();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
