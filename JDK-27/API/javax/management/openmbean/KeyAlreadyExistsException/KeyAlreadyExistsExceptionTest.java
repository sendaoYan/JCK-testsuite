/*
 * @test
 * @summary javax.management.openmbean.KeyAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main KeyAlreadyExistsExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.KeyAlreadyExistsException;

public class KeyAlreadyExistsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.KeyAlreadyExistsException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new KeyAlreadyExistsException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new KeyAlreadyExistsException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by TabularDataSupport.put for a duplicate calculated index
        try {
            javax.management.openmbean.CompositeType row =
                new javax.management.openmbean.CompositeType("Row", "d",
                    new String[]{"name"}, new String[]{"nd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.TabularType tt =
                new javax.management.openmbean.TabularType("T", "d", row, new String[]{"name"});
            javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
            td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"name"}, new Object[]{"x"}));
            td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"name"}, new Object[]{"x"}));
            throw new AssertionError("expected KeyAlreadyExistsException");
        } catch (KeyAlreadyExistsException ex) {
            check(ex.getMessage().contains("already refers to a value"), "thrown message");
        } catch (Exception ex) {
            throw new AssertionError("wrong exception " + ex);
        }
    }

    // ================= deepened (batch g): ctor chain, SUID, serialization, put/putAll/Map-view triggers =================
    static java.lang.Object roundTripG(java.lang.Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testConstructorsG() throws Exception {
        KeyAlreadyExistsException noArg = new KeyAlreadyExistsException();
        check(noArg.getMessage() == null, "G: no-arg message null");
        check(noArg.getCause() == null, "G: no-arg cause null");
        KeyAlreadyExistsException withMsg = new KeyAlreadyExistsException("boom");
        check("boom".equals(withMsg.getMessage()), "G: String ctor message");
        check(withMsg.getCause() == null, "G: String ctor cause null");
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "G: initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.KeyAlreadyExistsException"), "G: toString prefix");
        check(KeyAlreadyExistsException.class.getConstructors().length == 2, "G: exactly two public constructors () and (String)");
        check(java.lang.IllegalArgumentException.class == KeyAlreadyExistsException.class.getSuperclass(),
            "G: direct superclass IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(KeyAlreadyExistsException.class), "G: unchecked");
        check(java.io.Serializable.class.isAssignableFrom(KeyAlreadyExistsException.class), "G: Serializable");
    }
    static void testSerializationG() throws Exception {
        KeyAlreadyExistsException e2 = (KeyAlreadyExistsException) roundTripG(new KeyAlreadyExistsException("round-trip"));
        check(e2.getClass() == KeyAlreadyExistsException.class, "G: round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "G: round-trip message preserved");
        check(java.io.ObjectStreamClass.lookup(KeyAlreadyExistsException.class).getSerialVersionUID() == 1845183636745282866L,
            "G: serialVersionUID locked");
        KeyAlreadyExistsException n2 = (KeyAlreadyExistsException) roundTripG(new KeyAlreadyExistsException());
        check(n2.getMessage() == null, "G: round-trip null message preserved");
    }
    static void testTriggersG() throws Exception {
        javax.management.openmbean.CompositeType row = new javax.management.openmbean.CompositeType("Row", "d",
            new String[]{"k", "v"}, new String[]{"kd", "vd"},
            new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING, javax.management.openmbean.SimpleType.STRING});
        javax.management.openmbean.TabularType tt = new javax.management.openmbean.TabularType("T", "d", row, new String[]{"k"});
        javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
        // trigger 1: two rows in one putAll batch share the same calculated index (exact message)
        try {
            td.putAll(new javax.management.openmbean.CompositeData[]{
                new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"same", "v1"}),
                new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"same", "v2"})});
            throw new AssertionError("G: expected KeyAlreadyExistsException from duplicate index inside one putAll batch");
        } catch (KeyAlreadyExistsException e) {
            check("Argument elements values[1] and values[0] have the same indexes, calculated according to this TabularData instance's tabularType.".equals(e.getMessage()),
                "G: putAll within-batch duplicate exact message");
        }
        // trigger 2: putAll batch colliding with a row already in the table (exact message)
        td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"dup", "a1"}));
        try {
            td.putAll(new javax.management.openmbean.CompositeData[]{
                new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"other", "x"}),
                new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"dup", "y"})});
            throw new AssertionError("G: expected KeyAlreadyExistsException from putAll against existing row");
        } catch (KeyAlreadyExistsException e) {
            check("Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.".equals(e.getMessage()),
                "G: putAll against existing row exact message");
        }
        // trigger 3: the java.util.Map view put(Object[],CompositeData) throws too (no Map-style replace)
        try {
            td.put(new Object[]{"dup"}, new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"dup", "a3"}));
            throw new AssertionError("G: expected KeyAlreadyExistsException from Map-view put");
        } catch (KeyAlreadyExistsException e) {
            check("Argument value's index, calculated according to this TabularData instance's tabularType, already refers to a value in this table.".equals(e.getMessage()),
                "G: Map-view put exact message");
        }
        check("a1".equals(td.get(new Object[]{"dup"}).get("v")), "G: original value untouched by failed puts");
        check(td.size() == 1, "G: failed putAll left the table unchanged (validated before any mutation)");
        check(td.get(new Object[]{"other"}) == null, "G: non-colliding row of the failed batch was not inserted");
        // remove is the only path back to a free index
        td.remove(new Object[]{"dup"});
        td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"k", "v"}, new Object[]{"dup", "fresh"}));
        check("fresh".equals(td.get(new Object[]{"dup"}).get("v")), "G: put succeeds after remove");
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
