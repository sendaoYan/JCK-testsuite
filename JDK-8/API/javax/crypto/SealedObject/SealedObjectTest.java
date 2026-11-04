/*
 * @test
 * @summary javax.crypto.SealedObject conformance: constructor and getObject.
 * @run main SealedObjectTest
 */

public class SealedObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.NullCipher c = new javax.crypto.NullCipher();
        javax.crypto.SealedObject so = new javax.crypto.SealedObject("payload", c);
        check("payload".equals(so.getObject(new javax.crypto.NullCipher())), "getObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
