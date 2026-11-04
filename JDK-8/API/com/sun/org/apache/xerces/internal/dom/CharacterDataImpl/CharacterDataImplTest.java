/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.CharacterDataImpl conformance: abstract class hierarchy and members.
 * @run main CharacterDataImplTest
 */
public class CharacterDataImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.CharacterDataImpl", false, CharacterDataImplTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ChildNode"), "superclass com.sun.org.apache.xerces.internal.dom.ChildNode");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getNodeValue"), "method getNodeValue");
        check(mn.contains("setNodeValueInternal"), "method setNodeValueInternal");
        check(mn.contains("setNodeValue"), "method setNodeValue");
        check(mn.contains("getData"), "method getData");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("appendData"), "method appendData");
        check(mn.contains("deleteData"), "method deleteData");
        check(mn.contains("internalDeleteData"), "method internalDeleteData");
        check(mn.contains("insertData"), "method insertData");
        check(mn.contains("internalInsertData"), "method internalInsertData");
        check(mn.contains("replaceData"), "method replaceData");
        check(mn.contains("setData"), "method setData");
        check(mn.contains("substringData"), "method substringData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
