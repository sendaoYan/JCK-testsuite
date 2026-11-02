/*
 * @test
 * @summary javax.smartcardio.CardTerminals.State conformance: reflection shape checks.
 * @run main StateTest
 */

import java.lang.reflect.*;

public class StateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardTerminals$State");
        check(c != null, "javax.smartcardio.CardTerminals$State loads");
        check(c.getName().equals("javax.smartcardio.CardTerminals$State"), "binary name is javax.smartcardio.CardTerminals$State");
        check(Modifier.isPublic(c.getModifiers()), "javax.smartcardio.CardTerminals$State is public");
        check(c.isEnum(), "javax.smartcardio.CardTerminals$State is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 5, "5 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(set("ALL", "CARD_PRESENT", "CARD_ABSENT", "CARD_INSERTION", "CARD_REMOVAL")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "ALL") != null, "Enum.valueOf works");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
