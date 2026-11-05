/*
 * @test
 * @summary javax.management.openmbean.OpenDataException conformance: white-box structure + black-box behavior.
 * @run main OpenDataExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.JMException;
import javax.management.openmbean.OpenDataException;

public class OpenDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenDataException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(JMException.class.isAssignableFrom(c), "extends JMException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        Exception e = new OpenDataException("msg");
        check(e.getMessage().equals("msg"), "message");
        Exception e2 = new OpenDataException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by CompositeType for a duplicate item name
        try {
            new javax.management.openmbean.CompositeType("T", "d",
                new String[]{"a", "a"}, new String[]{"x", "y"},
                new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING, javax.management.openmbean.SimpleType.INTEGER});
            throw new AssertionError("expected OpenDataException");
        } catch (OpenDataException ex) {
            check(ex.getMessage().contains("duplicates a previous item names"), "thrown message");
        }
    }

    // ================= deepened (batch g): ctor chain, SUID, serialization, more real triggers =================
    static java.lang.Object roundTripG(java.lang.Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testConstructorsG() throws Exception {
        OpenDataException noArg = new OpenDataException();
        check(noArg.getMessage() == null, "G: no-arg message null");
        check(noArg.getCause() == null, "G: no-arg cause null");
        OpenDataException withMsg = new OpenDataException("boom");
        check("boom".equals(withMsg.getMessage()), "G: String ctor message");
        check(withMsg.getCause() == null, "G: String ctor cause null");
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "G: initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.OpenDataException"), "G: toString prefix");
        check(OpenDataException.class.getConstructors().length == 2, "G: exactly two public constructors () and (String)");
        check(javax.management.JMException.class == OpenDataException.class.getSuperclass(), "G: direct superclass JMException");
        check(!java.lang.RuntimeException.class.isAssignableFrom(OpenDataException.class), "G: checked exception (no RuntimeException ancestor)");
        check(java.io.Serializable.class.isAssignableFrom(OpenDataException.class), "G: Serializable");
    }
    static void testSerializationG() throws Exception {
        OpenDataException e2 = (OpenDataException) roundTripG(new OpenDataException("round-trip"));
        check(e2.getClass() == OpenDataException.class, "G: round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "G: round-trip message preserved");
        check(java.io.ObjectStreamClass.lookup(OpenDataException.class).getSerialVersionUID() == 8346311255433349870L,
            "G: serialVersionUID locked");
        OpenDataException n2 = (OpenDataException) roundTripG(new OpenDataException());
        check(n2.getMessage() == null, "G: round-trip null message preserved");
    }
    static void testTriggersG() throws Exception {
        javax.management.openmbean.CompositeType ct = new javax.management.openmbean.CompositeType("T", "d",
            new String[]{"a"}, new String[]{"ad"},
            new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.INTEGER});
        // trigger: item value of the wrong open type (exact message)
        try {
            new javax.management.openmbean.CompositeDataSupport(ct, new String[]{"a"}, new Object[]{"str"});
            throw new AssertionError("G: expected OpenDataException from wrong item value type");
        } catch (OpenDataException e) {
            check("Argument value of wrong type for item a: value str, type javax.management.openmbean.SimpleType(name=java.lang.Integer)".equals(e.getMessage()),
                "G: wrong-value trigger exact message");
        }
        // trigger: item names not matching the CompositeType
        try {
            new javax.management.openmbean.CompositeDataSupport(ct, new String[]{"b"}, new Object[]{1});
            throw new AssertionError("G: expected OpenDataException from item name mismatch");
        } catch (OpenDataException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Item names do not match CompositeType"),
                "G: item-name trigger message prefix");
        }
        // trigger: OpenMBeanParameterInfoSupport default value of wrong type
        @SuppressWarnings("rawtypes") javax.management.openmbean.OpenType rawInt = javax.management.openmbean.SimpleType.INTEGER;
        try {
            new javax.management.openmbean.OpenMBeanParameterInfoSupport("p", "d", rawInt, "str");
            throw new AssertionError("G: expected OpenDataException from info default value type");
        } catch (OpenDataException e) {
            check(e.getMessage() != null && e.getMessage().contains("defaultValue"), "G: info default-value trigger");
        }
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
