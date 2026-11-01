/*
 * @test
 * @summary java.io.OptionalDataException conformance: hierarchy and public fields.
 * @run main OptionalDataExceptionTest
 */

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OptionalDataExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOptionalDataException() throws Exception {
        check(IOException.class.isAssignableFrom(OptionalDataException.class),
              "extends IOException");
        check(ObjectStreamException.class.isAssignableFrom(OptionalDataException.class),
              "extends ObjectStreamException");
        Field length = OptionalDataException.class.getField("length");
        check(length.getType() == int.class, "length is int");
        check(Modifier.isPublic(length.getModifiers()), "length is public");
        Field eof = OptionalDataException.class.getField("eof");
        check(eof.getType() == boolean.class, "eof is boolean");
        check(Modifier.isPublic(eof.getModifiers()), "eof is public");
    }

    public static void main(String[] args) throws Exception {
        testOptionalDataException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
