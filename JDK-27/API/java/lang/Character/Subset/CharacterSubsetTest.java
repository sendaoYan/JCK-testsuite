/*
 * @test
 * @summary java.lang.Character.Subset conformance: white-box structure + black-box behavior.
 * @run main CharacterSubsetTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CharacterSubsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Character$Subset");
        check(Modifier.isPublic(c.getModifiers()), "Subset is public");
        check(!Modifier.isFinal(c.getModifiers()), "Subset is not final (UnicodeBlock extends it)");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isProtected(ctors[0].getModifiers()),
              "single protected constructor");
        for (Method m : new Method[]{c.getMethod("equals", Object.class),
                                     c.getMethod("hashCode"), c.getMethod("toString")}) {
            check(Modifier.isFinal(m.getModifiers()), m.getName() + " is final");
        }
        boolean hasName = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("name")) hasName = true;
        }
        check(hasName, "private 'name' field declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSubset() {
        Character.Subset s = new Character.Subset("test") {};
        check("test".equals(s.toString()), "toString returns the subset name");
        check(s.equals(s), "equals is reflexive");
        Character.Subset s2 = new Character.Subset("test") {};
        check(!s.equals(s2), "equals is identity-based (distinct anonymous subclasses differ)");
        check(s.hashCode() == s.hashCode(), "hashCode stable");
        Character.Subset latin = Character.UnicodeBlock.BASIC_LATIN;
        check(latin instanceof Character.Subset, "UnicodeBlock is a Subset");
        check("BASIC_LATIN".equals(latin.toString()), "UnicodeBlock toString is the constant name");
        check(Character.UnicodeBlock.of('A') == Character.UnicodeBlock.BASIC_LATIN, "of('A') via Subset constant");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSubset();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
