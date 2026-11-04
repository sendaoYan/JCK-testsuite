/*
 * @test
 * @summary java.lang.EnumConstantNotPresentException API conformance: enumType/constantName.
 * @run main EnumConstantNotPresentExceptionTest
 */

public class EnumConstantNotPresentExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Color { RED, GREEN }

    static void testEnumConstantNotPresentException() {
        EnumConstantNotPresentException e =
            new EnumConstantNotPresentException(Color.class, "BLUE");
        check(e instanceof RuntimeException, "extends RuntimeException");
        check("BLUE".equals(e.constantName()), "constantName");
        check(e.getMessage() != null && e.getMessage().contains("BLUE"),
              "message contains the constant name");
    }

    public static void main(String[] args) {
        testEnumConstantNotPresentException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
