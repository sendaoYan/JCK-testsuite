/*
 * @test
 * @summary javax.lang.model.util.AbstractTypeVisitorPreview conformance: white-box structure + black-box behavior.
 * @run main AbstractTypeVisitorPreviewTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class AbstractTypeVisitorPreviewTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.AbstractTypeVisitorPreview");
        check(c != null, "javax.lang.model.util.AbstractTypeVisitorPreview loads");
        check(c.getName().equals("javax.lang.model.util.AbstractTypeVisitorPreview"), "binary name is javax.lang.model.util.AbstractTypeVisitorPreview");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.util.AbstractTypeVisitorPreview is public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractTypeVisitor14"), "superclass is javax.lang.model.util.AbstractTypeVisitor14");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.util.AbstractTypeVisitorPreview");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("javax.lang.model.util.AbstractTypeVisitor14"), "extends AbstractTypeVisitor14");
        check(c.getDeclaredMethods().length == 0, "adds no methods over parent");
        check(c.getDeclaredConstructors().length == 1, "one constructor");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.lang.reflect.Method m = c.getMethod("visit", TypeMirror.class, Object.class);
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers()) && java.lang.reflect.Modifier.isFinal(m.getModifiers()), "inherits final public visit");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
