/*
 * @test
 * @summary java.util.Enumeration conformance: white-box structure + black-box behavior.
 * @run main EnumerationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class EnumerationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Enumeration");
        check(c.isInterface(), "is an interface");
        check(Modifier.isAbstract(c.getDeclaredMethod("hasMoreElements").getModifiers()),
                "hasMoreElements abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("nextElement").getModifiers()),
                "nextElement abstract");
        Method asIt = c.getDeclaredMethod("asIterator");
        check(!Modifier.isAbstract(asIt.getModifiers()), "asIterator default");
        check(asIt.getReturnType() == Iterator.class, "asIterator -> Iterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. hasMoreElements/nextElement traversal
        Enumeration<String> e = Collections.enumeration(Arrays.asList("a", "b", "c"));
        check(e.hasMoreElements(), "hasMoreElements");
        check("a".equals(e.nextElement()), "nextElement 1");
        check("b".equals(e.nextElement()), "nextElement 2");
        check("c".equals(e.nextElement()) && !e.hasMoreElements(), "nextElement 3 / exhausted");

        // 2. nextElement after exhaustion throws NoSuchElementException
        expectThrows(e::nextElement, NoSuchElementException.class, "nextElement after exhaustion");

        // 3. Vector.elements() + empty enumeration
        Enumeration<String> ve = new Vector<>(Arrays.asList("x")).elements();
        check("x".equals(ve.nextElement()), "Vector.elements");
        Enumeration<String> empty = Collections.enumeration(List.<String>of());
        check(!empty.hasMoreElements(), "empty enumeration has no elements");
        expectThrows(empty::nextElement, NoSuchElementException.class, "empty nextElement throws");

        // 4. asIterator() bridges to Iterator
        Enumeration<String> e4 = Collections.enumeration(Arrays.asList("m", "n"));
        Iterator<String> it = e4.asIterator();
        List<String> seen = new ArrayList<>();
        it.forEachRemaining(seen::add);
        check(seen.equals(Arrays.asList("m", "n")), "asIterator delegates to the enumeration");
        expectThrows(it::remove, UnsupportedOperationException.class, "asIterator.remove unsupported");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
