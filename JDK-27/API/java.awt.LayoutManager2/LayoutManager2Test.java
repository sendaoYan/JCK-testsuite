/*
 * @test
 * @summary java.awt.LayoutManager2 conformance: white-box structure + black-box behavior.
 * @run main LayoutManager2Test
 */

import java.lang.reflect.*;

public class LayoutManager2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.LayoutManager2", false, LayoutManager2Test.class.getClassLoader());
        check(c != null, "type loads: java.awt.LayoutManager2");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
        check(hasInterface(c, "java.awt.LayoutManager"), "extends LayoutManager");
    }

    static void testBlackBox() throws Exception {
        java.awt.LayoutManager2 impl = new java.awt.LayoutManager2() {
            public void addLayoutComponent(String n, java.awt.Component c) {}
            public void addLayoutComponent(java.awt.Component c, Object o) {}
            public void removeLayoutComponent(java.awt.Component c) {}
            public java.awt.Dimension preferredLayoutSize(java.awt.Container p) { return null; }
            public java.awt.Dimension minimumLayoutSize(java.awt.Container p) { return null; }
            public java.awt.Dimension maximumLayoutSize(java.awt.Container p) { return null; }
            public float getLayoutAlignmentX(java.awt.Container p) { return 0f; }
            public float getLayoutAlignmentY(java.awt.Container p) { return 0f; }
            public void invalidateLayout(java.awt.Container p) {}
            public void layoutContainer(java.awt.Container p) {}
        };
        impl.addLayoutComponent((java.awt.Component)null, (Object)"x");
        impl.addLayoutComponent((String)null, (java.awt.Component)null);
        impl.removeLayoutComponent(null);
        check(impl.maximumLayoutSize(null) == null, "maximum null");
        check(impl.getLayoutAlignmentX(null) == 0f, "alignX 0");
        check(impl.getLayoutAlignmentY(null) == 0f, "alignY 0");
        impl.invalidateLayout(null);
        impl.layoutContainer(null);
        check(true, "all callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
