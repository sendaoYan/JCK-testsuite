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


    // ===================== 契约深化：asIterator 适配器/remove 语义 =====================
    static void testEnumerationContracts() {
        java.util.List<String> seen = new java.util.ArrayList<String>();
        Enumeration<String> e = Collections.enumeration(java.util.Arrays.asList("x", "y"));
        while (e.hasMoreElements()) seen.add(e.nextElement());
        check(seen.size() == 2 && "x".equals(seen.get(0)), "enumeration order preserved");
        Iterator<String> it = Collections.enumeration(java.util.Arrays.asList("a", "b")).asIterator();
        java.util.List<String> via = new java.util.ArrayList<String>();
        while (it.hasNext()) via.add(it.next());
        check(via.size() == 2 && "a".equals(via.get(0)) && "b".equals(via.get(1)),
              "asIterator adapts enumeration in order");
        try {
            it.remove();
            check(false, "asIterator().remove() UOE");
        } catch (UnsupportedOperationException uoe) {
            check(true, "asIterator().remove() UOE");
        }
    }

    // ===================== 契约深化：自定义枚举探针（含 null 元素）/空枚举/Vector 非 fail-fast/asIterator 细节 =====================

    /** 固定序列（含一个 null 元素）的自定义枚举探针。 */
    static Enumeration<String> fixedEnumeration(final String[] items) {
        return new Enumeration<String>() {
            int i = 0;
            public boolean hasMoreElements() { return i < items.length; }
            public String nextElement() {
                if (i >= items.length) throw new NoSuchElementException("Enumerator is empty");
                return items[i++];
            }
        };
    }

    static void testCustomProbe() {
        Enumeration<String> e = fixedEnumeration(new String[] {"x", null, "z"});
        check(e.hasMoreElements() && e.hasMoreElements(), "hasMoreElements stable/idempotent");
        check("x".equals(e.nextElement()), "probe element 1");
        check(e.nextElement() == null, "probe element 2 is null (nulls allowed as values)");
        check("z".equals(e.nextElement()), "probe element 3");
        check(!e.hasMoreElements(), "probe exhausted");
        expectThrows(e::nextElement, NoSuchElementException.class, "exhausted nextElement NSEE");
        check(!e.hasMoreElements(), "still exhausted after NSEE");
    }

    static void testEmptyEnumeration() {
        Enumeration<String> e = Collections.<String>emptyEnumeration();
        check(!e.hasMoreElements() && !e.hasMoreElements(), "empty enumeration has nothing");
        expectThrows(e::nextElement, NoSuchElementException.class, "empty nextElement NSEE");
        Enumeration<String> e2 = Collections.enumeration(new ArrayList<String>());
        check(!e2.hasMoreElements(), "enumeration(empty list) is empty");
    }

    static void testVectorElements() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        List<String> seen = new ArrayList<>();
        Enumeration<String> e = v.elements();
        while (e.hasMoreElements()) seen.add(e.nextElement());
        check(seen.equals(Arrays.asList("a", "b", "c")), "Vector.elements preserves order");
        expectThrows(() -> new Vector<String>().elements().nextElement(),
              NoSuchElementException.class, "empty Vector elements NSEE");
        // 契约：Vector.elements() 返回的枚举不做 fail-fast 检测（实测锁定，与 JDK-8 一致）
        Vector<String> v2 = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e2 = v2.elements();
        check("a".equals(e2.nextElement()), "drain first element");
        v2.add("d");                      // 结构性修改
        check("b".equals(e2.nextElement()), "enumeration NOT fail-fast (continues after mod)");
        check("c".equals(e2.nextElement()) && "d".equals(e2.nextElement()),
              "enumeration sees remaining elements");
    }

    static void testEnumerationOrder() {
        List<String> src = new ArrayList<>(Arrays.asList("p", "q", "r"));
        Enumeration<String> e = Collections.enumeration(src);
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check("pqr".contentEquals(sb), "Collections.enumeration preserves list order");
    }

    static void testAsIteratorDetails() {
        // asIterator 是活适配器：逐个委托 hasMoreElements/nextElement
        Enumeration<String> e = fixedEnumeration(new String[] {"m", "n", "o"});
        Iterator<String> it = e.asIterator();
        check(it.hasNext(), "asIterator hasNext delegates");
        check("m".equals(it.next()), "asIterator next 1");
        check(it.hasNext() && "n".equals(it.next()), "asIterator next 2");
        // forEachRemaining 走枚举剩余元素
        List<String> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        check(rest.equals(Arrays.asList("o")), "asIterator forEachRemaining drains rest");
        check(!it.hasNext(), "asIterator exhausted");
        expectThrows(it::next, NoSuchElementException.class, "asIterator next after drain NSEE");
        // remove 无状态可删 → UOE
        Iterator<String> rm = fixedEnumeration(new String[] {"x"}).asIterator();
        rm.next();
        expectThrows(rm::remove, UnsupportedOperationException.class, "asIterator.remove UOE");
        // 适配器与原枚举共享位置：直接消耗枚举后迭代器看到的位置同步
        Enumeration<String> shared = fixedEnumeration(new String[] {"a", "b"});
        Iterator<String> ad = shared.asIterator();
        check("a".equals(ad.next()), "adapter consumes shared enumeration");
        check("b".equals(shared.nextElement()), "enumeration position advanced by adapter");
        check(!shared.hasMoreElements(), "shared enumeration drained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumerationContracts();
        testCustomProbe();
        testEmptyEnumeration();
        testVectorElements();
        testEnumerationOrder();
        testAsIteratorDetails();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
