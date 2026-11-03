/*
 * @test
 * @summary java.lang.LiveStackFrame.PrimitiveSlot conformance: white-box structure.
 *          (The owning interface LiveStackFrame and the constructor are package-private,
 *          so instances cannot be created outside java.lang; behavioral coverage is
 *          therefore limited to structural reflection.)
 * @run main PrimitiveSlotTest
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PrimitiveSlotTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame$PrimitiveSlot");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && !Modifier.isPublic(ctors[0].getModifiers()),
              "single package-private constructor (not instantiable outside java.lang)");
        Method size = c.getDeclaredMethod("size");
        check(Modifier.isPublic(size.getModifiers()) && Modifier.isAbstract(size.getModifiers()),
              "size() is public abstract returning int");
        check(size.getReturnType() == int.class, "size() returns int");
        for (String n : new String[]{"intValue", "longValue"}) {
            Method m = c.getDeclaredMethod(n);
            check(Modifier.isPublic(m.getModifiers()) && !Modifier.isAbstract(m.getModifiers()),
                  n + "() is public and concrete");
        }
        check(c.getDeclaredMethod("intValue").getReturnType() == int.class, "intValue() returns int");
        check(c.getDeclaredMethod("longValue").getReturnType() == long.class, "longValue() returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    // LiveStackFrame is package-private and PrimitiveSlot's constructor is package-private,
    // so no instance can be created or subclassed from an unnamed-module test; the class is
    // exercised by the JDK-internal StackWalker with ExtendedOption.LOCALS_AND_OPERANDS.
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame$PrimitiveSlot");
        check(c.getClassLoader() == null, "loaded by the bootstrap loader (java.lang)");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "plain abstract class");
        check(c.getPackageName().equals("java.lang"), "package java.lang");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
