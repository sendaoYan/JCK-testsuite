/*
 * @test
 * @summary java.nio.charset.MalformedInputException conformance: white-box structure + black-box length/message.
 * @run main MalformedInputExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;

public class MalformedInputExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.MalformedInputException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(c.getConstructor(int.class) != null, "int constructor present");
    }

    static void testBlackBox() {
        MalformedInputException e = new MalformedInputException(5);
        check(e.getInputLength() == 5, "getInputLength");
        check("Input length = 5".equals(e.getMessage()), "getMessage");
        check(e instanceof CharacterCodingException, "is CharacterCodingException");
        check(e.getClass().getSuperclass() == CharacterCodingException.class, "superclass is CharacterCodingException");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
