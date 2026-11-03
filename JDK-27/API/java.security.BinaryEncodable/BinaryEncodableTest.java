/*
 * @test
 * @summary java.security.BinaryEncodable conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main BinaryEncodableTest
 */

import java.lang.reflect.Modifier;
import java.security.BinaryEncodable;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class BinaryEncodableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.BinaryEncodable");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
    }

    static void testBlackBox() throws Exception {
        check(new PKCS8EncodedKeySpec(new byte[]{1, 2, 3}) instanceof BinaryEncodable, "PKCS8EncodedKeySpec is BinaryEncodable");
        check(new X509EncodedKeySpec(new byte[]{1, 2, 3}) instanceof BinaryEncodable, "X509EncodedKeySpec is BinaryEncodable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
