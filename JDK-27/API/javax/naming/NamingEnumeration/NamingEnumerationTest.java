/*
 * @test
 * @summary javax.naming.NamingEnumeration conformance: white-box structure + black-box behavior.
 * @run main NamingEnumerationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class NamingEnumerationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class ListEnum<T> implements NamingEnumeration<T> {
        final List<T> items; int pos; boolean closed;
        ListEnum(List<T> items) { this.items = new ArrayList<>(items); }
        public T next() throws NamingException { if (!hasMore()) throw new NoSuchElementException(); return items.get(pos++); }
        public boolean hasMore() throws NamingException { if (closed) throw new NamingException("enumeration closed"); return pos < items.size(); }
        public T nextElement() { try { return next(); } catch (NamingException e) { throw new NoSuchElementException(e.getMessage()); } }
        public boolean hasMoreElements() { return pos < items.size(); }
        public void close() throws NamingException { closed = true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NamingEnumeration.class;
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(java.util.Enumeration.class.isAssignableFrom(c), "extends java.util.Enumeration");
        for (String m : new String[] {"next", "hasMore", "close"})
            check(c.getMethod(m) != null, "declares " + m);
        check(c.getMethod("next").getExceptionTypes().length == 1
                && c.getMethod("next").getExceptionTypes()[0] == NamingException.class, "next throws NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ListEnum<String> en = new ListEnum<>(List.of("a", "b"));
        check(en.hasMore(), "hasMore initially");
        check(en.next().equals("a"), "next a");
        check(en.hasMoreElements(), "legacy hasMoreElements");
        check(en.nextElement().equals("b"), "legacy nextElement");
        check(!en.hasMore(), "exhausted");
        try { en.next(); throw new AssertionError("next past end"); }
        catch (NoSuchElementException e) { check(true, "NoSuchElementException after exhaustion"); }

        en.close();
        try { en.hasMore(); throw new AssertionError("hasMore after close"); }
        catch (NamingException e) { check(e.getMessage().equals("enumeration closed"), "post-close NamingException"); }

        // iteration pattern used across JNDI
        ListEnum<Integer> nums = new ListEnum<>(List.of(1, 2, 3));
        int sum = 0;
        while (nums.hasMore()) sum += nums.next();
        check(sum == 6, "iterate and sum");
        nums.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
