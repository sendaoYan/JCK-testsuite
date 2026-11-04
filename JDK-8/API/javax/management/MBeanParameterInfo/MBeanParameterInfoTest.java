/*
 * @test
 * @summary javax.management.MBeanParameterInfo conformance: full contract
 *          coverage. name/type/description accessors, descriptor ctor +
 *          null -> empty default, equals participation matrix (name/type/
 *          description/descriptor), hashCode = name+type only, toString
 *          format, clone, serialization round-trip.
 * @run main MBeanParameterInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanParameterInfo;

public class MBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        check("x".equals(p.getName()), "name");
        check("int".equals(p.getType()), "type");
        check("param".equals(p.getDescription()), "description");
    }

    static void testDescriptorAndEquals() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param",
            new ImmutableDescriptor(new String[]{"units"}, new Object[]{"ms"}));
        check("ms".equals(p.getDescriptor().getFieldValue("units")), "descriptor");
        MBeanParameterInfo q = new MBeanParameterInfo("x", "int", "param");
        MBeanParameterInfo r = new MBeanParameterInfo("x", "int", "param");
        check(q.equals(r) && q.hashCode() == r.hashCode(), "equals/hashCode");
        check(!q.equals(p), "different descriptor -> not equal");
        MBeanParameterInfo c = (MBeanParameterInfo) q.clone();
        check(q.equals(c), "clone equals");
    }

    // ---- deepened: equals participation matrix ------------------------------

    static void testEqualsParticipationMatrix() {
        MBeanParameterInfo base = new MBeanParameterInfo("x", "int", "d");
        check(!base.equals(new MBeanParameterInfo("x", "long", "d")), "type participates in equals");
        check(!base.equals(new MBeanParameterInfo("y", "int", "d")), "name participates in equals");
        check(!base.equals(new MBeanParameterInfo("x", "int", "OTHER")), "description participates in equals");
        check(!base.equals(new MBeanParameterInfo("x", "int", "d",
                new ImmutableDescriptor("k=v"))), "descriptor participates in equals");
        check(!base.equals(null) && !base.equals("x"), "equals null/foreign false");
        // hashCode covers only name and type: different description -> same hash
        check(base.hashCode() == new MBeanParameterInfo("x", "int", "OTHER").hashCode(),
                "hashCode uses only name and type");
    }

    // ---- deepened: null descriptor default + toString ------------------------

    static void testNullDescriptorAndToString() {
        MBeanParameterInfo plain = new MBeanParameterInfo("x", "int", "d");
        check(plain.getDescriptor() != null && plain.getDescriptor().getFieldNames().length == 0,
                "3-arg ctor yields empty descriptor");
        check(plain.getDescriptor() instanceof ImmutableDescriptor, "default descriptor is ImmutableDescriptor");
        String s = plain.toString();
        check(s.contains("name=x") && s.contains("type=int") && s.contains("description=d"),
                "toString carries name/type/description");
        check(s.startsWith("javax.management.MBeanParameterInfo["), "toString prefix");
    }

    // ---- deepened: serialization round-trip ---------------------------------

    static void testSerialization() throws Exception {
        MBeanParameterInfo original = new MBeanParameterInfo("x", "int", "d",
                new ImmutableDescriptor("units=ms"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanParameterInfo read = (MBeanParameterInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("int".equals(read.getType()), "type survives serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testDescriptorAndEquals();
        testEqualsParticipationMatrix();
        testNullDescriptorAndToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
