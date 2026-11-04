/*
 * @test
 * @summary java.util.IllegalFormatException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatExceptionTest
 */

import java.lang.reflect.Modifier;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;

public class IllegalFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.IllegalFormatException");
        check(c.getSuperclass() == IllegalArgumentException.class,
                "extends IllegalArgumentException");
        check(c.getConstructors().length == 0, "no public constructor");
    }

    static void testBlackBox() {
        Object[] subs = {
            new DuplicateFormatFlagsException(","),
            new FormatFlagsConversionMismatchException("#", 's'),
            new IllegalFormatCodePointException(0x110000),
            new IllegalFormatConversionException('d', String.class),
            new IllegalFormatFlagsException("+"),
            new IllegalFormatPrecisionException(2),
            new IllegalFormatWidthException(-1),
            new MissingFormatArgumentException("%s"),
            new MissingFormatWidthException("%-d"),
            new UnknownFormatConversionException("y"),
            new UnknownFormatFlagsException("!"),
        };
        for (Object o : subs) {
            check(o instanceof IllegalFormatException, o.getClass().getSimpleName()
                    + " is IllegalFormatException");
            check(o instanceof IllegalArgumentException, o.getClass().getSimpleName()
                    + " is IllegalArgumentException");
        }
        boolean caught = false;
        try {
            throw new MissingFormatArgumentException("%s");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        check(caught, "catchable as IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
