/*
 * @test
 * @summary java.util.Enumeration API conformance: hasMoreElements/nextElement, and
 *          Vector.elements.
 * @run main EnumerationTest
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class EnumerationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEnumeration() {
        Enumeration<String> e = Collections.enumeration(Arrays.asList("a", "b", "c"));
        check(e.hasMoreElements(), "hasMoreElements");
        check("a".equals(e.nextElement()), "nextElement 1");
        check("b".equals(e.nextElement()), "nextElement 2");
        check("c".equals(e.nextElement()) && !e.hasMoreElements(), "nextElement 3 / exhausted");
        Enumeration<String> ve = new Vector<>(Arrays.asList("x")).elements();
        check("x".equals(ve.nextElement()), "Vector.elements");
    }


    // ===================== 契约深化：asIterator（9+，反射探测）/序列契约 =====================
    static void testEnumerationContracts() {
        java.util.List<String> seen = new java.util.ArrayList<String>();
        Enumeration<String> e = Collections.enumeration(java.util.Arrays.asList("x", "y"));
        while (e.hasMoreElements()) seen.add(e.nextElement());
        check(seen.size() == 2 && "x".equals(seen.get(0)), "enumeration order preserved");
        try {
            java.lang.reflect.Method m = Enumeration.class.getMethod("asIterator");
            Iterator<String> it = (Iterator<String>) m.invoke(
                  Collections.enumeration(java.util.Arrays.asList("a", "b")));
            java.util.List<String> via = new java.util.ArrayList<String>();
            while (it.hasNext()) via.add(it.next());
            check(via.size() == 2 && "a".equals(via.get(0)), "asIterator adapts enumeration");
        } catch (NoSuchMethodException nsme) {
            check(true, "asIterator absent on JDK 8 (added in 9)");
        } catch (Exception ex) {
            check(false, "asIterator invocation failed");
        }
    }

    // ===================== 契约深化：自定义枚举探针（含 null 元素）/空枚举/Vector 非 fail-fast/序保持 =====================

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
        try {
            e.nextElement();
            check(false, "exhausted nextElement must throw NSEE");
        } catch (NoSuchElementException expected) {
            check(true, "exhausted nextElement NSEE");
        }
        check(!e.hasMoreElements(), "still exhausted after NSEE");
    }

    static void testEmptyEnumeration() {
        Enumeration<String> e = Collections.<String>emptyEnumeration();
        check(!e.hasMoreElements() && !e.hasMoreElements(), "empty enumeration has nothing");
        try {
            e.nextElement();
            check(false, "empty nextElement must throw NSEE");
        } catch (NoSuchElementException expected) {
            check(true, "empty nextElement NSEE");
        }
        // Collections.enumeration(空集合) 同样为空
        Enumeration<String> e2 = Collections.enumeration(java.util.Collections.<String>emptyList());
        check(!e2.hasMoreElements(), "enumeration(empty list) is empty");
    }

    static void testVectorElements() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        java.util.List<String> seen = new java.util.ArrayList<String>();
        Enumeration<String> e = v.elements();
        while (e.hasMoreElements()) seen.add(e.nextElement());
        check(seen.equals(Arrays.asList("a", "b", "c")), "Vector.elements preserves order");
        // 空 Vector 的枚举
        try {
            new Vector<String>().elements().nextElement();
            check(false, "empty Vector elements NSEE");
        } catch (NoSuchElementException expected) {
            check(true, "empty Vector elements NSEE");
        }
        // 契约：Vector.elements() 返回的枚举不做 fail-fast 检测——
        // 结构性修改后继续 nextElement 不抛 CME，仍能取到剩余元素（实测锁定）
        Vector<String> v2 = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e2 = v2.elements();
        check("a".equals(e2.nextElement()), "drain first element");
        v2.add("d");                      // 结构性修改
        check("b".equals(e2.nextElement()), "enumeration NOT fail-fast (continues after mod)");
        check("c".equals(e2.nextElement()) && "d".equals(e2.nextElement()),
              "enumeration sees remaining elements");
    }

    static void testEnumerationOrder() {
        // Collections.enumeration 保持列表的 encounter order
        java.util.List<String> src = new java.util.ArrayList<String>(
              java.util.Arrays.asList("p", "q", "r"));
        Enumeration<String> e = Collections.enumeration(src);
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check("pqr".contentEquals(sb), "Collections.enumeration preserves list order");
    }

    public static void main(String[] args) {
        testEnumeration();
        testEnumerationContracts();
        testCustomProbe();
        testEmptyEnumeration();
        testVectorElements();
        testEnumerationOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
