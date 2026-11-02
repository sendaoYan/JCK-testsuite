/*
 * @test
 * @summary javax.accessibility.AccessibleEditableText conformance: interface reflection.
 * @run main AccessibleEditableTextTest
 */

public class AccessibleEditableTextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleEditableText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleText"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setTextContents"), "method setTextContents");
        check(hasMethod(c, "insertTextAtIndex"), "method insertTextAtIndex");
        check(hasMethod(c, "getTextRange"), "method getTextRange");
        check(hasMethod(c, "delete"), "method delete");
        check(hasMethod(c, "cut"), "method cut");
        check(hasMethod(c, "paste"), "method paste");
        check(hasMethod(c, "replaceText"), "method replaceText");
        check(hasMethod(c, "selectText"), "method selectText");
        check(hasMethod(c, "setAttributes"), "method setAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
