/*
 * @test
 * @summary javax.xml.stream.events.Characters conformance: interface hierarchy and methods.
 * @run main CharactersTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class CharactersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.events.Characters");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getData"), "has method getData");
        check(names.contains("isWhiteSpace"), "has method isWhiteSpace");
        check(names.contains("isCData"), "has method isCData");
        check(names.contains("isIgnorableWhiteSpace"), "has method isIgnorableWhiteSpace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
