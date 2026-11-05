/*
 * @test
 * @summary javax.management.BadStringOperationException conformance: full
 *          contract coverage. operation-derived toString (getMessage stays
 *          null because the op is not passed to the superclass) and Exception
 *          hierarchy.
 * @run main BadStringOperationExceptionTest
 */

import javax.management.BadStringOperationException;

public class BadStringOperationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        BadStringOperationException x = new BadStringOperationException("-");
        check("BadStringOperationException: -".equals(x.toString()), "toString");
        check(x.getMessage() == null, "getMessage null (op not message)");
        check(x instanceof Exception, "extends Exception");
        check(x.getCause() == null, "no cause");
        testBSOContract();
        testBSORealTriggerAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): op-derived toString, real trigger from string
    //      arithmetic, serialization, serialVersionUID ----

    static void testBSOContract() {
        BadStringOperationException x = new BadStringOperationException("minus");
        check("BadStringOperationException: minus".equals(x.toString()), "toString op-derived");
        check(x.getMessage() == null, "op is not the detail message");
        check(x instanceof Exception && x instanceof java.io.Serializable, "Exception + Serializable");
        check(x.getCause() == null, "no cause");
        check("BadStringOperationException: null".equals(new BadStringOperationException(null).toString()),
                "null op toString");
    }

    static void testBSORealTriggerAndSerialization() throws Exception {
        try {
            javax.management.Query.minus(javax.management.Query.value("a"), javax.management.Query.value("b")).apply(null);
            check(false, "expected BadStringOperationException from minus on strings");
        } catch (BadStringOperationException e) {
            passed++;
        }
        try {
            javax.management.Query.div(javax.management.Query.value("a"), javax.management.Query.value("b")).apply(null);
            check(false, "expected BadStringOperationException from div on strings");
        } catch (BadStringOperationException e) {
            passed++;
        }
        long uid = java.io.ObjectStreamClass.lookup(BadStringOperationException.class).getSerialVersionUID();
        check(uid == 7802201238441662100L, "serialVersionUID locked: " + uid);
        BadStringOperationException back =
                (BadStringOperationException) roundTripBS(new BadStringOperationException("minus"));
        check("BadStringOperationException: minus".equals(back.toString()), "round-trip keeps op");
    }

    static Object roundTripBS(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }
}
