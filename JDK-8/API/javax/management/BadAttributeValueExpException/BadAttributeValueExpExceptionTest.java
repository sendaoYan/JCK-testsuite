/*
 * @test
 * @summary javax.management.BadAttributeValueExpException conformance: full
 *          contract coverage. value-derived toString, null value handling,
 *          getMessage stays null (value is not the message), and Exception
 *          hierarchy.
 * @run main BadAttributeValueExpExceptionTest
 */

import javax.management.BadAttributeValueExpException;

public class BadAttributeValueExpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        BadAttributeValueExpException x = new BadAttributeValueExpException("msg");
        check(x instanceof Exception, "extends Exception");
        check("BadAttributeValueException: msg".equals(x.toString()), "toString value-derived");
        check(x.getMessage() == null, "getMessage null (value not message)");

        BadAttributeValueExpException n = new BadAttributeValueExpException(null);
        check(n.toString().endsWith("null"), "null value toString");

        BadAttributeValueExpException num = new BadAttributeValueExpException(Integer.valueOf(42));
        check(num.toString().endsWith("42"), "numeric value toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
