/*
 * @test
 * @summary javax.management.openmbean.InvalidKeyException conformance: white-box structure + black-box behavior.
 * @run main InvalidKeyExceptionTest
 */

import java.lang.reflect.Modifier;

public class InvalidKeyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidKeyException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new javax.management.openmbean.InvalidKeyException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new javax.management.openmbean.InvalidKeyException();
        check(e2.getMessage() == null, "null message no-arg");
        // behavioral: thrown by CompositeDataSupport.get for an unknown key
        try {
            new javax.management.openmbean.CompositeDataSupport(
                new javax.management.openmbean.CompositeType("T", "d",
                    new String[]{"a"}, new String[]{"da"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING}),
                new String[]{"a"}, new Object[]{"v"}).get("missing");
            throw new AssertionError("expected InvalidKeyException");
        } catch (javax.management.openmbean.InvalidKeyException ex) {
            check(ex.getMessage().contains("missing"), "thrown message contains key");
        } catch (Exception ex) {
            throw new AssertionError("wrong exception " + ex);
        }
    }

    // ================= deepened (batch g): ctor chain, SUID, serialization, exact trigger messages =================
    static java.lang.Object roundTripG(java.lang.Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testConstructorsG() throws Exception {
        javax.management.openmbean.InvalidKeyException noArg = new javax.management.openmbean.InvalidKeyException();
        check(noArg.getMessage() == null, "G: no-arg message null");
        check(noArg.getCause() == null, "G: no-arg cause null");
        javax.management.openmbean.InvalidKeyException withMsg = new javax.management.openmbean.InvalidKeyException("boom");
        check("boom".equals(withMsg.getMessage()), "G: String ctor message");
        check(withMsg.getCause() == null, "G: String ctor cause null");
        RuntimeException root = new RuntimeException("root");
        withMsg.initCause(root);
        check(withMsg.getCause() == root, "G: initCause sets cause once");
        check(withMsg.toString().startsWith("javax.management.openmbean.InvalidKeyException"), "G: toString prefix");
        check(javax.management.openmbean.InvalidKeyException.class.getConstructors().length == 2,
            "G: exactly two public constructors () and (String)");
        check(java.lang.IllegalArgumentException.class == javax.management.openmbean.InvalidKeyException.class.getSuperclass(),
            "G: direct superclass IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(javax.management.openmbean.InvalidKeyException.class), "G: unchecked");
        check(java.io.Serializable.class.isAssignableFrom(javax.management.openmbean.InvalidKeyException.class), "G: Serializable");
        check(!javax.management.openmbean.KeyAlreadyExistsException.class.isAssignableFrom(javax.management.openmbean.InvalidKeyException.class),
            "G: not a KeyAlreadyExistsException (sibling under IAE)");
    }
    static void testSerializationG() throws Exception {
        javax.management.openmbean.InvalidKeyException e2 =
            (javax.management.openmbean.InvalidKeyException) roundTripG(new javax.management.openmbean.InvalidKeyException("round-trip"));
        check(e2.getClass() == javax.management.openmbean.InvalidKeyException.class, "G: round-trip exact class");
        check("round-trip".equals(e2.getMessage()), "G: round-trip message preserved");
        check(java.io.ObjectStreamClass.lookup(javax.management.openmbean.InvalidKeyException.class).getSerialVersionUID()
            == 4224269443946322062L, "G: serialVersionUID locked");
        javax.management.openmbean.InvalidKeyException n2 =
            (javax.management.openmbean.InvalidKeyException) roundTripG(new javax.management.openmbean.InvalidKeyException());
        check(n2.getMessage() == null, "G: round-trip null message preserved");
    }
    static void testTriggersG() throws Exception {
        javax.management.openmbean.CompositeDataSupport cd = new javax.management.openmbean.CompositeDataSupport(
            new javax.management.openmbean.CompositeType("T", "d",
                new String[]{"a"}, new String[]{"ad"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.INTEGER}),
            new String[]{"a"}, new Object[]{1});
        // exact trigger message for a key that is not an item name
        try {
            cd.get("missing");
            throw new AssertionError("G: expected InvalidKeyException from get(missing)");
        } catch (javax.management.openmbean.InvalidKeyException e) {
            check("Argument key=\"missing\" is not an existing item name for this CompositeData instance.".equals(e.getMessage()),
                "G: get(missing) exact message");
        }
        // contrast: get(null) is a plain IllegalArgumentException with its own message
        try {
            cd.get(null);
            throw new AssertionError("G: expected IllegalArgumentException from get(null)");
        } catch (IllegalArgumentException e) {
            check(!(e instanceof javax.management.openmbean.InvalidKeyException), "G: get(null) is plain IAE");
            check("Argument key cannot be a null or empty String.".equals(e.getMessage()), "G: get(null) exact message");
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
