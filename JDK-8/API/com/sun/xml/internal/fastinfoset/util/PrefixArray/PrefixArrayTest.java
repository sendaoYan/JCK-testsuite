/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.util.PrefixArray conformance: class hierarchy and members.
 * @run main PrefixArrayTest
 */

public class PrefixArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.util.PrefixArray", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.util.PrefixArray"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.util.ValueArray".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.util.ValueArray");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("clearCompletely"), "has method clearCompletely");
        check(mn.contains("clearDeclarationIds"), "has method clearDeclarationIds");
        check(mn.contains("countNamespacePool"), "has method countNamespacePool");
        check(mn.contains("countPrefixPool"), "has method countPrefixPool");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getArray"), "has method getArray");
        check(mn.contains("getCompleteArray"), "has method getCompleteArray");
        check(mn.contains("getNamespaceFromPrefix"), "has method getNamespaceFromPrefix");
        check(mn.contains("getPrefixFromNamespace"), "has method getPrefixFromNamespace");
        check(mn.contains("getPrefixes"), "has method getPrefixes");
        check(mn.contains("getPrefixesFromNamespace"), "has method getPrefixesFromNamespace");
        check(mn.contains("increaseNamespacePool"), "has method increaseNamespacePool");
        check(mn.contains("increasePrefixPool"), "has method increasePrefixPool");
        check(mn.contains("initializeEntries"), "has method initializeEntries");
        check(mn.contains("popScope"), "has method popScope");
        check(mn.contains("popScopeWithPrefixEntry"), "has method popScopeWithPrefixEntry");
        check(mn.contains("pushScope"), "has method pushScope");
        check(mn.contains("pushScopeWithPrefixEntry"), "has method pushScopeWithPrefixEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
