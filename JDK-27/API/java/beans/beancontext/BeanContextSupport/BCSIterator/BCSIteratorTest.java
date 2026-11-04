/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextSupport.BCSIterator conformance: white-box structure + black-box behavior.
 * @run main BCSIteratorTest
 */

import java.lang.reflect.Modifier;
import java.util.Iterator;

public class BCSIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport$BCSIterator");
        int m = c.getModifiers();
        check(Modifier.isProtected(m), "protected class");
        check(Modifier.isFinal(m), "final class");
        check(Modifier.isStatic(m), "static class");
        check(Iterator.class.isAssignableFrom(c), "implements Iterator");
    }

    static void testBlackBox() throws Exception {
        java.beans.beancontext.BeanContextSupport bc = new java.beans.beancontext.BeanContextSupport();
        bc.add("a");
        Iterator it = bc.iterator();
        check("java.beans.beancontext.BeanContextSupport$BCSIterator".equals(it.getClass().getName()), "iterator is BCSIterator");
        check(it.hasNext(), "hasNext");
        check("a".equals(it.next()), "next");
        it.remove(); // no-op per BeanContextSupport source
        check(bc.size() == 1 && bc.contains("a"), "remove is a no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
