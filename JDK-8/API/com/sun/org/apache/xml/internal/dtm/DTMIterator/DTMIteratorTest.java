/*
 * @test
 * @summary com.sun.org.apache.xml.internal.dtm.DTMIterator conformance: interface hierarchy and members.
 * @run main DTMIteratorTest
 */
public class DTMIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.dtm.DTMIterator", false, DTMIteratorTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDTM"), "method getDTM");
        check(mn.contains("getDTMManager"), "method getDTMManager");
        check(mn.contains("getRoot"), "method getRoot");
        check(mn.contains("setRoot"), "method setRoot");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("getWhatToShow"), "method getWhatToShow");
        check(mn.contains("getExpandEntityReferences"), "method getExpandEntityReferences");
        check(mn.contains("nextNode"), "method nextNode");
        check(mn.contains("previousNode"), "method previousNode");
        check(mn.contains("detach"), "method detach");
        check(mn.contains("allowDetachToRelease"), "method allowDetachToRelease");
        check(mn.contains("getCurrentNode"), "method getCurrentNode");
        check(mn.contains("isFresh"), "method isFresh");
        check(mn.contains("setShouldCacheNodes"), "method setShouldCacheNodes");
        check(mn.contains("isMutable"), "method isMutable");
        check(mn.contains("getCurrentPos"), "method getCurrentPos");
        check(mn.contains("runTo"), "method runTo");
        check(mn.contains("setCurrentPos"), "method setCurrentPos");
        check(mn.contains("item"), "method item");
        check(mn.contains("setItem"), "method setItem");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("cloneWithReset"), "method cloneWithReset");
        check(mn.contains("clone"), "method clone");
        check(mn.contains("isDocOrdered"), "method isDocOrdered");
        check(mn.contains("getAxis"), "method getAxis");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
