/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport.BCSIterator conformance: white-box structure + black-box behavior.
 *          Structure: protected static final nested class implementing Iterator with a
 *          single package-private ctor taking java.util.Iterator. Behavior through
 *          BeanContextSupport.iterator(): exact contents, empty iterator (hasNext false,
 *          next -> NoSuchElementException), remove() is a documented no-op (even before
 *          next(), deviating from the Iterator contract), repeated remove safe, fail-fast
 *          CME after structural add or remove, iteration does not mutate the context,
 *          and independent iterator instances per call.
 * @run main BCSIteratorTest
 */

import java.lang.reflect.Modifier;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Arrays;

public class BCSIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport$BCSIterator");
        int m = c.getModifiers();
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(m), "concrete class");
        check(Modifier.isProtected(m), "protected class");
        check(Modifier.isFinal(m), "final class");
        check(Modifier.isStatic(m), "static class");
        check(Iterator.class.isAssignableFrom(c), "implements Iterator");
        check("java.beans.beancontext.BeanContextSupport".equals(c.getDeclaringClass().getName()),
              "declared inside BeanContextSupport");
        check(c.getName().endsWith("BeanContextSupport$BCSIterator"), "binary name is nested");
        // single package-private ctor taking one java.util.Iterator
        check(c.getDeclaredConstructors().length == 1, "single declared ctor");
        java.lang.reflect.Constructor<?> ct = c.getDeclaredConstructors()[0];
        check(!Modifier.isPublic(ct.getModifiers()) && !Modifier.isProtected(ct.getModifiers()), "ctor package-private");
        Class<?>[] params = ct.getParameterTypes();
        check(params.length == 1 && params[0] == Iterator.class, "ctor takes a single java.util.Iterator");
        // public Iterator surface
        boolean hasHasNext = false, hasNextM = false, hasRemove = false;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) {
            if (mm.getName().equals("hasNext")) hasHasNext = true;
            if (mm.getName().equals("next")) hasNextM = true;
            if (mm.getName().equals("remove")) hasRemove = true;
        }
        check(hasHasNext && hasNextM && hasRemove, "declares hasNext/next/remove");
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

    static void testIterationContract() throws Exception {
        java.beans.beancontext.BeanContextSupport bc = new java.beans.beancontext.BeanContextSupport();
        // empty iterator
        Iterator<Object> empty = bc.iterator();
        check(!empty.hasNext(), "empty context iterator hasNext false");
        checkThrows(NoSuchElementException.class, new ThrowingRunnable() {
            public void run() { new java.beans.beancontext.BeanContextSupport().iterator().next(); } },
            "next on empty context -> NoSuchElementException");
        // exact contents
        bc.add("c1"); bc.add("c2"); bc.add("c3");
        Set<Object> seen = new HashSet<Object>();
        for (Object o : bc.toArray()) seen.add(o);
        Set<Object> iterated = new HashSet<Object>();
        Iterator<Object> it = bc.iterator();
        while (it.hasNext()) iterated.add(it.next());
        check(iterated.equals(new HashSet<Object>(Arrays.asList("c1", "c2", "c3"))), "iterator yields exactly added children");
        check(iterated.equals(seen), "iterator matches toArray contents");
        check(bc.size() == 3, "iteration does not remove children");
        // hasNext is idempotent (does not advance)
        Iterator<Object> it2 = bc.iterator();
        check(it2.hasNext() && it2.hasNext() && it2.hasNext(), "hasNext idempotent");
        check(it2.next() != null, "next still returns first element after repeated hasNext");
        // independent instances per call
        Iterator<Object> it3 = bc.iterator();
        check(it3 != it2 && it3.hasNext(), "each iterator() call returns a fresh instance");
        // remove is a no-op even before next() (no IllegalStateException, unlike Iterator contract)
        Iterator<Object> it4 = bc.iterator();
        it4.remove(); // must not throw IllegalStateException
        check(true, "remove before next is a no-op (no IllegalStateException)");
        it4.next();
        it4.remove();
        it4.remove(); // repeated remove also safe
        check(bc.size() == 3, "repeated no-op remove leaves children untouched");
        // iterating to exhaustion
        Iterator<Object> it5 = bc.iterator();
        int n = 0;
        while (it5.hasNext()) { it5.next(); n++; }
        check(n == 3, "iteration yields exactly 3 elements");
        check(!it5.hasNext(), "exhausted iterator hasNext false");
        checkThrows(NoSuchElementException.class, new ThrowingRunnable() {
            public void run() {
                java.beans.beancontext.BeanContextSupport b = new java.beans.beancontext.BeanContextSupport();
                b.add("z");
                Iterator<Object> i = b.iterator();
                i.next();
                i.next(); // past the end
            } }, "next past the end -> NoSuchElementException");
    }

    static void testFailFast() throws Exception {
        // structural add during iteration -> CME on next
        final java.beans.beancontext.BeanContextSupport bc1 = new java.beans.beancontext.BeanContextSupport();
        bc1.add("f1");
        Iterator<Object> it1 = bc1.iterator();
        bc1.add("f2");
        checkThrows(ConcurrentModificationException.class, new ThrowingRunnable() {
            public void run() { it1.next(); } }, "next after concurrent add -> CME");
        // structural remove during iteration -> CME on next
        final java.beans.beancontext.BeanContextSupport bc2 = new java.beans.beancontext.BeanContextSupport();
        bc2.add("g1"); bc2.add("g2");
        Iterator<Object> it2 = bc2.iterator();
        it2.next();
        bc2.remove("g2");
        checkThrows(ConcurrentModificationException.class, new ThrowingRunnable() {
            public void run() { it2.next(); } }, "next after concurrent remove -> CME");
        check(bc2.size() == 1, "context intact after CME probe");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIterationContract();
        testFailFast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
