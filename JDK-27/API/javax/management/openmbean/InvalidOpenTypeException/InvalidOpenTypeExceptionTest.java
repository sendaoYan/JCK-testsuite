/*
 * @test
 * @summary javax.management.openmbean.InvalidOpenTypeException conformance: white-box structure + black-box behavior.
 * @run main InvalidOpenTypeExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.InvalidOpenTypeException;

public class InvalidOpenTypeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidOpenTypeException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new InvalidOpenTypeException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new InvalidOpenTypeException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by TabularDataSupport.calculateIndex for a wrong row type
        try {
            javax.management.openmbean.CompositeType goodRow =
                new javax.management.openmbean.CompositeType("Row", "d",
                    new String[]{"name"}, new String[]{"nd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.TabularType tt =
                new javax.management.openmbean.TabularType("T", "d", goodRow, new String[]{"name"});
            javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
            javax.management.openmbean.CompositeType badRow =
                new javax.management.openmbean.CompositeType("Other", "d",
                    new String[]{"x"}, new String[]{"dx"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.CompositeDataSupport bad =
                new javax.management.openmbean.CompositeDataSupport(badRow, new String[]{"x"}, new Object[]{"v"});
            td.calculateIndex(bad);
            throw new AssertionError("expected InvalidOpenTypeException");
        } catch (InvalidOpenTypeException ex) {
            check(ex.getMessage().contains("is not assignable"), "thrown message");
        } catch (Exception ex) {
            throw new AssertionError("wrong exception " + ex);
        }
    }

    // ================= deepened (batch g): ctor chain, SUID, serialization, put row-type trigger =================
    static java.lang.Object roundTripG(java.lang.Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testConstructorsG() throws Exception {
        InvalidOpenTypeException noArg = new InvalidOpenTypeException();
        check(noArg.getMessage() == null, "G: no-arg message null");
        check(noArg.getCause() == null, "G: no-arg cause null");
        InvalidOpenTypeException withMsg = new InvalidOpenTypeException("boom");
        check("boom".equals(withMsg.getMessage()), "G: String ctor message");
        check(withMsg.getCause() == null, "G: String ctor cause null");
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "G: initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.InvalidOpenTypeException"), "G: toString prefix");
        check(InvalidOpenTypeException.class.getConstructors().length == 2, "G: exactly two public constructors () and (String)");
        check(java.lang.IllegalArgumentException.class == InvalidOpenTypeException.class.getSuperclass(),
            "G: direct superclass IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(InvalidOpenTypeException.class), "G: unchecked");
        check(java.io.Serializable.class.isAssignableFrom(InvalidOpenTypeException.class), "G: Serializable");
        // not related to KeyAlreadyExistsException or InvalidKeyException (all siblings under IAE)
        check(!javax.management.openmbean.KeyAlreadyExistsException.class.isAssignableFrom(InvalidOpenTypeException.class),
            "G: not a KeyAlreadyExistsException");
    }
    static void testSerializationG() throws Exception {
        InvalidOpenTypeException e2 = (InvalidOpenTypeException) roundTripG(new InvalidOpenTypeException("round-trip"));
        check(e2.getClass() == InvalidOpenTypeException.class, "G: round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "G: round-trip message preserved");
        check(java.io.ObjectStreamClass.lookup(InvalidOpenTypeException.class).getSerialVersionUID() == -2837312755412327534L,
            "G: serialVersionUID locked");
        InvalidOpenTypeException n2 = (InvalidOpenTypeException) roundTripG(new InvalidOpenTypeException());
        check(n2.getMessage() == null, "G: round-trip null message preserved");
    }
    static void testTriggersG() throws Exception {
        // trigger: TabularDataSupport.put with a row whose CompositeType differs from the row type (exact message shape)
        javax.management.openmbean.CompositeType rowA = new javax.management.openmbean.CompositeType("A", "d",
            new String[]{"k"}, new String[]{"kd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
        javax.management.openmbean.CompositeType rowB = new javax.management.openmbean.CompositeType("B", "d",
            new String[]{"k"}, new String[]{"kd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
        javax.management.openmbean.TabularType tt = new javax.management.openmbean.TabularType("T", "d", rowA, new String[]{"k"});
        javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
        try {
            td.put(new javax.management.openmbean.CompositeDataSupport(rowB, new String[]{"k"}, new Object[]{"v"}));
            throw new AssertionError("G: expected InvalidOpenTypeException from mismatched row type");
        } catch (InvalidOpenTypeException e) {
            check(e.getMessage() != null
                && e.getMessage().startsWith("Argument value's composite type [")
                && e.getMessage().contains("is not assignable to this TabularData instance's row type [")
                && e.getMessage().endsWith("]."),
                "G: row-type trigger message shape");
        }
        check(td.size() == 0, "G: rejected row not stored");
        check(td.containsKey(new Object[]{"v"}) == false, "G: table still lacks the rejected key");
        // OpenType.isValue is the underlying predicate and simply reports false (no exception)
        check(!javax.management.openmbean.SimpleType.INTEGER.isValue("not-an-integer"), "G: isValue false for wrong type");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorsG();
        testSerializationG();
        testTriggersG();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
