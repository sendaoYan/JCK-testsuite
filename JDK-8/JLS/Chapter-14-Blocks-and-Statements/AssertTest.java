/*
 * @test
 * @summary JLS 14.10 (Java SE 8): with assertions enabled, a false condition
 *          throws AssertionError and a true condition does nothing; a detail
 *          expression becomes the exception's message.
 * @run main/othervm -ea AssertTest
 */

public class AssertTest {
    public static void main(String[] args) {
        assert 1 + 1 == 2 : "should pass";

        boolean thrown = false;
        try {
            assert false : "boom";
        } catch (AssertionError e) {
            thrown = true;
            if (!"boom".equals(e.getMessage())) throw new AssertionError("detail message");
        }
        if (!thrown) throw new AssertionError("AssertionError expected");
        System.out.println("PASS");
    }
}
