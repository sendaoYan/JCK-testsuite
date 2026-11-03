/*
 * @test
 * @summary javax.lang.model.util.Elements.DocCommentKind conformance: white-box structure + black-box behavior.
 * @run main DocCommentKindTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class DocCommentKindTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.Elements$DocCommentKind");
        check(c != null, "javax.lang.model.util.Elements$DocCommentKind loads");
        check(c.getName().equals("javax.lang.model.util.Elements$DocCommentKind"), "binary name is javax.lang.model.util.Elements$DocCommentKind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.Elements$DocCommentKind is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 2, "2 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("END_OF_LINE", "TRADITIONAL")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "END_OF_LINE") != null, "Enum.valueOf works");
    }

    static void testBlackBox() throws Exception {
        Elements.DocCommentKind[] vals = Elements.DocCommentKind.values();
        check(vals.length == 2, "2 enum constants");
        check(Elements.DocCommentKind.valueOf("TRADITIONAL") == Elements.DocCommentKind.TRADITIONAL, "valueOf TRADITIONAL");
        check(Elements.DocCommentKind.END_OF_LINE != Elements.DocCommentKind.TRADITIONAL, "constants distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
