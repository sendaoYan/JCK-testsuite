/*
 * @test
 * @summary java.lang.instrument.ClassDefinition conformance: white-box structure + black-box behavior.
 * @run main ClassDefinitionTest
 */
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Modifier;

public class ClassDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.instrument.ClassDefinition");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getConstructor(Class.class, byte[].class) != null, "constructor (Class, byte[])");
        check(c.getMethod("getDefinitionClass").getReturnType() == Class.class, "getDefinitionClass returns Class");
        check(c.getMethod("getDefinitionClassFile").getReturnType() == byte[].class, "getDefinitionClassFile returns byte[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        byte[] bytes = {1, 2, 3, 4, 5};
        ClassDefinition cd = new ClassDefinition(String.class, bytes);
        check(cd.getDefinitionClass() == String.class, "getDefinitionClass returns the class");
        byte[] got = cd.getDefinitionClassFile();
        check(got.length == bytes.length, "getDefinitionClassFile length preserved");
        boolean equal = true;
        for (int i = 0; i < bytes.length; i++) equal &= (got[i] == bytes[i]);
        check(equal, "getDefinitionClassFile content preserved");

        ClassDefinition empty = new ClassDefinition(Integer.class, new byte[0]);
        check(empty.getDefinitionClass() == Integer.class, "empty class file still keeps class");
        check(empty.getDefinitionClassFile().length == 0, "empty class file length 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
