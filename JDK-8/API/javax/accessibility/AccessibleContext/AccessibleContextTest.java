/*
 * @test
 * @summary javax.accessibility.AccessibleContext conformance: abstract class reflection.
 * @run main AccessibleContextTest
 */

public class AccessibleContextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleContext");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleName"), "method getAccessibleName");
        check(hasMethod(c, "setAccessibleName"), "method setAccessibleName");
        check(hasMethod(c, "getAccessibleDescription"), "method getAccessibleDescription");
        check(hasMethod(c, "setAccessibleDescription"), "method setAccessibleDescription");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getAccessibleParent"), "method getAccessibleParent");
        check(hasMethod(c, "setAccessibleParent"), "method setAccessibleParent");
        check(hasMethod(c, "getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        check(hasMethod(c, "getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(hasMethod(c, "getAccessibleChild"), "method getAccessibleChild");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        check(hasMethod(c, "getAccessibleAction"), "method getAccessibleAction");
        check(hasMethod(c, "getAccessibleComponent"), "method getAccessibleComponent");
        check(hasMethod(c, "getAccessibleSelection"), "method getAccessibleSelection");
        check(hasMethod(c, "getAccessibleText"), "method getAccessibleText");
        check(hasMethod(c, "getAccessibleEditableText"), "method getAccessibleEditableText");
        check(hasMethod(c, "getAccessibleValue"), "method getAccessibleValue");
        check(hasMethod(c, "getAccessibleIcon"), "method getAccessibleIcon");
        check(hasMethod(c, "getAccessibleRelationSet"), "method getAccessibleRelationSet");
        check(hasMethod(c, "getAccessibleTable"), "method getAccessibleTable");
        check(hasMethod(c, "firePropertyChange"), "method firePropertyChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
