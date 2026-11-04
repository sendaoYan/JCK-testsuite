/*
 * @test
 * @summary java.util.Collection conformance: white-box structure + black-box behavior.
 * @run main CollectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CollectionTest {
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
        Class<?> c = Class.forName("java.util.Collection");
        check(c.isInterface(), "is an interface");
        check(Iterable.class.isAssignableFrom(c), "extends Iterable");
        c.getDeclaredMethod("size");
        c.getDeclaredMethod("isEmpty");
        c.getDeclaredMethod("contains", Object.class);
        c.getDeclaredMethod("iterator");
        c.getDeclaredMethod("toArray");
        c.getDeclaredMethod("toArray", Object[].class);
        c.getDeclaredMethod("add", Object.class);
        c.getDeclaredMethod("remove", Object.class);
        c.getDeclaredMethod("containsAll", Collection.class);
        c.getDeclaredMethod("addAll", Collection.class);
        c.getDeclaredMethod("removeAll", Collection.class);
        c.getDeclaredMethod("retainAll", Collection.class);
        c.getDeclaredMethod("clear");
        c.getDeclaredMethod("equals", Object.class);
        c.getDeclaredMethod("hashCode");
        check(true, "declares core members");
        check(Modifier.isAbstract(c.getDeclaredMethod("size").getModifiers()), "size abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("iterator").getModifiers()), "iterator abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()), "add abstract");
        // default methods
        check(!Modifier.isAbstract(c.getDeclaredMethod("removeIf", Predicate.class).getModifiers()),
                "removeIf default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("spliterator").getModifiers()), "spliterator default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("stream").getModifiers()), "stream default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("parallelStream").getModifiers()), "parallelStream default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("toArray",
                java.util.function.IntFunction.class).getModifiers()), "toArray(IntFunction) default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. basics
        Collection<String> c = new ArrayList<>();
        check(c.isEmpty() && c.size() == 0, "empty");
        check(c.add("a") && c.add("b"), "add returns true");
        check(c.size() == 2, "size");
        check(c.contains("a") && !c.contains("z"), "contains");
        check(c.remove("a"), "remove returns true");
        check(!c.remove("z"), "remove missing returns false");
        check(c.size() == 1, "size after remove");
        c.clear();
        check(c.isEmpty(), "clear");

        // 2. bulk
        Collection<String> c2 = new ArrayList<>(Arrays.asList("a"));
        Collection<String> d = new ArrayList<>(Arrays.asList("x", "y"));
        check(c2.addAll(d), "addAll returns true");
        check(c2.containsAll(d), "containsAll");
        check(c2.removeAll(d), "removeAll returns true");
        check(!c2.contains("x") && c2.contains("a"), "removeAll removed the elements");

        // 3. toArray overloads
        Collection<String> e = new ArrayList<>(Arrays.asList("p", "q"));
        Object[] arr = e.toArray();
        check(arr.length == 2 && "p".equals(arr[0]), "toArray()");
        String[] typed = e.toArray(new String[0]);
        check(typed.length == 2 && "q".equals(typed[1]), "toArray(T[])");
        String[] viaIntFn = e.toArray(String[]::new);
        check(viaIntFn.length == 2 && "p".equals(viaIntFn[0]), "toArray(IntFunction)");

        // 4. iteration + defaults
        Collection<String> e2 = new ArrayList<>(Arrays.asList("p", "q"));
        Iterator<String> it = e2.iterator();
        check(it.hasNext() && it.next().equals("p") && it.next().equals("q") && !it.hasNext(),
                "iterator");
        final List<String> seen = new ArrayList<>();
        e2.forEach(seen::add);
        check(seen.equals(new ArrayList<>(Arrays.asList("p", "q"))), "forEach");
        e2.removeIf(s -> s.equals("p"));
        check(e2.equals(new ArrayList<>(Arrays.asList("q"))), "removeIf");

        // 5. stream/parallelStream
        Collection<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3));
        Stream<Integer> st = nums.stream();
        check(st.mapToInt(Integer::intValue).sum() == 6, "stream");
        check(nums.parallelStream().mapToInt(Integer::intValue).sum() == 6, "parallelStream");

        // 6. spliterator
        Spliterator<Integer> sp = nums.spliterator();
        check(sp.estimateSize() == 3 && sp.getExactSizeIfKnown() == 3, "spliterator sized");
        final int[] sum = {0};
        sp.forEachRemaining(n -> sum[0] += n);
        check(sum[0] == 6, "spliterator forEachRemaining");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
