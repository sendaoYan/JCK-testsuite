/*
 * @test
 * @summary java.awt.dnd.DropTargetContext conformance: reflected type shape.
 * @run main DropTargetContextTest
 */

import java.lang.reflect.*;

public class DropTargetContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.dnd.DropTargetContext", false, DropTargetContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(),
              "public/protected/member");
        if (c.isEnum()) {
            check(c.getEnumConstants() != null, "enum has constants");
        } else {
            check(c.getEnumConstants() == null, "non-enum returns null constants");
        }
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) {
            check(Modifier.isInterface(m), "interface modifier consistent");
        } else if (c != Object.class) {
            check(c.getSuperclass() != null, "class superclass non-null");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
