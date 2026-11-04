/*
 * @test
 * @summary java.util.IllegalFormatException conformance: package-private root of the
 *          format-specifier exception hierarchy; cannot be instantiated directly;
 *          Formatter throws its concrete subclasses with exact types; unchecked.
 * @run main IllegalFormatExceptionTest
 */

import java.io.Serializable;
import java.util.Formatter;
import java.util.IllegalFormatException;

public class IllegalFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("java.util.IllegalFormatException");
        check(c.getSuperclass() == IllegalArgumentException.class,
              "extends IllegalArgumentException");
        check(RuntimeException.class.isAssignableFrom(c), "unchecked RuntimeException");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        // 构造器全部包私有：外部无法直接 new
        int pub = 0;
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(k.getModifiers())) pub++;
        }
        check(pub == 0, "no public constructors (package-private only)");
    }

    static void testSubclassFamily() throws Exception {
        // 11 个具体子类均属于本族
        String[] subs = {
            "DuplicateFormatFlagsException", "FormatFlagsConversionMismatchException",
            "IllegalFormatCodePointException", "IllegalFormatConversionException",
            "IllegalFormatFlagsException", "IllegalFormatPrecisionException",
            "IllegalFormatWidthException", "MissingFormatArgumentException",
            "MissingFormatWidthException", "UnknownFormatConversionException",
            "UnknownFormatFlagsException" };
        for (String s : subs) {
            Class<?> k = Class.forName("java.util." + s);
            check(IllegalFormatException.class.isAssignableFrom(k), s + " is an IllegalFormatException");
            check(IllegalArgumentException.class.isAssignableFrom(k), s + " is unchecked");
        }
        check(subs.length == 11, "family member count");
    }

    static void testFormatterThrowsFamily() {
        Formatter f = new Formatter();
        // 触发后捕获的具体类型都是 IllegalFormatException 族
        Class<? extends Throwable>[] expected = new Class[] {
            java.util.IllegalFormatConversionException.class,
            java.util.UnknownFormatConversionException.class,
            java.util.MissingFormatArgumentException.class,
            java.util.IllegalFormatFlagsException.class,
            java.util.DuplicateFormatFlagsException.class,
            java.util.IllegalFormatPrecisionException.class,
            java.util.IllegalFormatWidthException.class,
            java.util.MissingFormatWidthException.class };
        Runnable[] triggers = new Runnable[] {
            () -> f.format("%d", "x"),
            () -> f.format("%q", 1),
            () -> f.format("%s and %s", "one"),
            () -> f.format("%+ d", 1),
            () -> f.format("%,,d", 1),
            () -> f.format("%5.3d", 1),
            () -> f.format("%5n"),
            () -> f.format("%-d", 1) };
        for (int i = 0; i < triggers.length; i++) {
            try {
                triggers[i].run();
                check(false, "trigger " + i + " should throw");
            } catch (IllegalFormatException e) {
                check(expected[i].isInstance(e), "trigger " + i + " yields " + expected[i].getSimpleName());

            }
        }
        f.close();
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testSubclassFamily();
        testFormatterThrowsFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
