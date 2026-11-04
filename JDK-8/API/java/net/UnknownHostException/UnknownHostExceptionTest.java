/*
 * @test
 * @summary java.net.UnknownHostException conformance: message and hierarchy.
 * @run main UnknownHostExceptionTest
 */


public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.UnknownHostException e = new java.net.UnknownHostException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.io.IOException, "is IOException");
        java.net.UnknownHostException e0 = new java.net.UnknownHostException();
        check(e0.getMessage() == null, "no-arg message null");
        // ------------------------------------------------------------------
        // Deepening: hierarchy, serialization, null/throw semantics
        // ------------------------------------------------------------------
        check(e.getCause() == null, "no cause by default");
        check(e0.getCause() == null, "no-arg no cause");
        check("java.net.UnknownHostException: boom".equals(e.toString()), "toString exact");
        // null message constructor
        java.net.UnknownHostException en = new java.net.UnknownHostException(null);
        check(en.getMessage() == null, "null message stays null");
        // catchable as IOException / Exception
        try {
            throw e;
        } catch (java.io.IOException ioe) {
            check(ioe == e, "catchable as IOException, same object");
        }
        try {
            throw e0;
        } catch (Exception ex) {
            check(ex == e0, "catchable as Exception, same object");
        }
        check(java.io.Serializable.class.isAssignableFrom(java.net.UnknownHostException.class),
                "Serializable via IOException");
        // serialization round trip preserves class and message
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.writeObject(e0);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        java.net.UnknownHostException back = (java.net.UnknownHostException) ois.readObject();
        java.net.UnknownHostException back0 = (java.net.UnknownHostException) ois.readObject();
        check(back != null && "boom".equals(back.getMessage()), "serialized message preserved");
        check(back.getClass() == java.net.UnknownHostException.class, "serialized exact class");
        check(back instanceof java.io.IOException, "serialized still IOException");
        check(back0.getMessage() == null, "serialized no-arg message null");
        check(!back.equals(e0), "distinct instances");
        // produced by the InetAddress API with a malformed literal (no DNS)
        try {
            java.net.InetAddress.getByName("999.888.777.666");
            check(false, "malformed literal must throw");
        } catch (java.net.UnknownHostException uhe) {
            check(uhe.getMessage() != null, "UHE from getByName has message");
            check(uhe.getMessage().contains("999.888.777.666"), "message contains the literal");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
