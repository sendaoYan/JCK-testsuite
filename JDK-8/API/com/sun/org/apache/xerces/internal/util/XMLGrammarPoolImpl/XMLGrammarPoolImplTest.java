/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl conformance: class hierarchy and members.
 * @run main XMLGrammarPoolImplTest
 */
public class XMLGrammarPoolImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl", false, XMLGrammarPoolImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool"), "has interface com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("retrieveInitialGrammarSet"), "method retrieveInitialGrammarSet");
        check(mn.contains("cacheGrammars"), "method cacheGrammars");
        check(mn.contains("retrieveGrammar"), "method retrieveGrammar");
        check(mn.contains("putGrammar"), "method putGrammar");
        check(mn.contains("getGrammar"), "method getGrammar");
        check(mn.contains("removeGrammar"), "method removeGrammar");
        check(mn.contains("containsGrammar"), "method containsGrammar");
        check(mn.contains("lockPool"), "method lockPool");
        check(mn.contains("unlockPool"), "method unlockPool");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
