/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.ParentNode conformance: abstract class hierarchy and members.
 * @run main ParentNodeTest
 */
public class ParentNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.ParentNode", false, ParentNodeTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.ChildNode"), "superclass com.sun.org.apache.xerces.internal.dom.ChildNode");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cloneNode"), "method cloneNode");
        check(mn.contains("getOwnerDocument"), "method getOwnerDocument");
        check(mn.contains("ownerDocument"), "method ownerDocument");
        check(mn.contains("setOwnerDocument"), "method setOwnerDocument");
        check(mn.contains("hasChildNodes"), "method hasChildNodes");
        check(mn.contains("getChildNodes"), "method getChildNodes");
        check(mn.contains("getFirstChild"), "method getFirstChild");
        check(mn.contains("getLastChild"), "method getLastChild");
        check(mn.contains("lastChild"), "method lastChild");
        check(mn.contains("insertBefore"), "method insertBefore");
        check(mn.contains("internalInsertBefore"), "method internalInsertBefore");
        check(mn.contains("removeChild"), "method removeChild");
        check(mn.contains("internalRemoveChild"), "method internalRemoveChild");
        check(mn.contains("replaceChild"), "method replaceChild");
        check(mn.contains("getTextContent"), "method getTextContent");
        check(mn.contains("hasTextContent"), "method hasTextContent");
        check(mn.contains("setTextContent"), "method setTextContent");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("getChildNodesUnoptimized"), "method getChildNodesUnoptimized");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("isEqualNode"), "method isEqualNode");
        check(mn.contains("setReadOnly"), "method setReadOnly");
        check(mn.contains("synchronizeChildren"), "method synchronizeChildren");
        check(mn.contains("checkNormalizationAfterInsert"), "method checkNormalizationAfterInsert");
        check(mn.contains("checkNormalizationAfterRemove"), "method checkNormalizationAfterRemove");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
