/*
 * @test
 * @summary jdk.jshell.Wrap.CompoundWrap conformance: compound wrap reflection.
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED CompoundWrapTest
 */

import java.lang.reflect.*;

public class CompoundWrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: CompoundWrap composition shape ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$CompoundWrap");
        check(c.getSuperclass().getName().equals("jdk.jshell.Wrap"), "extends Wrap");
        // Wrap implements GeneralWrap: wrapped() plus position mapping
        Class<?> gw = Class.forName("jdk.jshell.GeneralWrap");
        for (String n : new String[]{"wrapped", "snippetIndexToWrapIndex", "wrapIndexToSnippetIndex",
                "firstSnippetIndex", "lastSnippetIndex", "snippetLineToWrapLine"})
            check(hasMethod(gw, n) || hasMethod(c, n), "has " + n);
        // static factories on Wrap produce usable wraps
        Class<?> w = Class.forName("jdk.jshell.Wrap");
        for (String n : new String[]{"methodWrap", "methodReturnWrap", "methodUnreachableSemiWrap", "methodUnreachableWrap", "varWrap"})
            check(hasMethod(w, n), "Wrap factory " + n);
        // methodWrap output really contains the method body
        java.lang.reflect.Method factory = w.getMethod("methodWrap", String.class);
        factory.setAccessible(true);
        Object mw = factory.invoke(null, "return 42;");
        check(mw != null, "methodWrap returns wrap");
        java.lang.reflect.Method wrappedM = mw.getClass().getMethod("wrapped");
        wrappedM.setAccessible(true);
        Object wrapped = wrappedM.invoke(mw);
        check(wrapped instanceof String && ((String) wrapped).contains("return 42;"), "wrapped contains body");
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$CompoundWrap");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.Wrap"), "extends Wrap");
        check(c.getMethod("wrapped") != null, "wrapped method");
        testShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
