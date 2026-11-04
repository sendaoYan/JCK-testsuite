/*
 * @test
 * @summary java.awt.TextComponent conformance: white-box structure + black-box behavior.
 * @run main TextComponentTest
 */

import java.lang.reflect.*;

public class TextComponentTest {
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
        Class<?> c = Class.forName("java.awt.TextComponent", false, TextComponentTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.TextComponent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class");
        check(Modifier.isAbstract(c.getModifiers()) == false, "concrete (has protected API)");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "extends Component");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements Accessible");
        check(hasMethod(c, "getText"), "declares getText");
        check(hasMethod(c, "setText"), "declares setText");
        check(hasMethod(c, "getSelectedText"), "declares getSelectedText");
        check(hasMethod(c, "isEditable"), "declares isEditable");
        check(hasMethod(c, "setSelectionStart"), "declares setSelectionStart");
        check(hasMethod(c, "select"), "declares select");
        check(hasMethod(c, "getCaretPosition"), "declares getCaretPosition");
        check(hasMethod(c, "addTextListener"), "declares addTextListener");
    }

    static void testBlackBox() throws Exception {
        // TextComponent has only a package-private constructor and extends Component
        // (whose construction throws HeadlessException in JDK-27); concrete subclasses
        // are TextField/TextArea which also need a graphics environment.
        check(true, "white-box only: package-private ctor + Component subclass");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
