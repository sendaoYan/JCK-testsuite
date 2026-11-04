/*
 * @test
 * @summary sun.security.pkcs.ParsingException conformance: type shape + behavioral (headless-safe).
 * @run main ParsingExceptionTest
 */

public class ParsingExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.pkcs.ParsingException", false, ParsingExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.IOException"), "superclass");
        sun.security.pkcs.ParsingException pe = new sun.security.pkcs.ParsingException("parse");
        check(pe.getMessage() != null, "ParsingException message");
        check(pe instanceof java.io.IOException, "ParsingException instanceof IOException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
