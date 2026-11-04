/*
 * @test
 * @summary java.util.List conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main ListTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SequencedCollection;

public class ListTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.List");
        check(c.isInterface(), "is an interface");
        check(SequencedCollection.class.isAssignableFrom(c), "extends SequencedCollection");
        check(Modifier.isAbstract(c.getDeclaredMethod("get", int.class).getModifiers()), "get abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("set", int.class, Object.class).getModifiers()),
                "set abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("add", int.class, Object.class).getModifiers()),
                "add abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("remove", int.class).getModifiers()),
                "remove abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("indexOf", Object.class).getModifiers()),
                "indexOf abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("size").getModifiers()), "size abstract");
        c.getDeclaredMethod("of");
        c.getDeclaredMethod("copyOf", java.util.Collection.class);
        c.getDeclaredMethod("ofLazy", int.class, java.util.function.IntFunction.class);
    }

    static void testBlackBox() {
        check(List.of().isEmpty(), "of() empty");
        check(List.of("a", "b").equals(Arrays.asList("a", "b")), "of varargs");
        check(List.of("a", "a").size() == 2, "of allows duplicates");
        expectThrows(() -> List.of("a", (String) null), NullPointerException.class, "of rejects null");

        List<String> c = List.copyOf(Arrays.asList("x", "y"));
        check(c.equals(Arrays.asList("x", "y")), "copyOf");

        List<String> a = new ArrayList<>(Arrays.asList("a", "b", "c"));
        check("a".equals(a.getFirst()) && "c".equals(a.getLast()), "getFirst/getLast");
        a.addFirst("z");
        check("z".equals(a.get(0)), "addFirst");
        check("z".equals(a.removeFirst()), "removeFirst");
        check(a.equals(Arrays.asList("a", "b", "c")), "back to original");

        List<Integer> nums = new ArrayList<>(Arrays.asList(3, 1, 2));
        nums.sort(Integer::compareTo);
        check(nums.equals(Arrays.asList(1, 2, 3)), "sort");
        nums.replaceAll(i -> i * 10);
        check(nums.equals(Arrays.asList(10, 20, 30)), "replaceAll");
        check(nums.subList(1, 3).equals(Arrays.asList(20, 30)), "subList");

        // sort(null) uses natural ordering; removeIf filters in place
        List<Integer> nat = new ArrayList<>(Arrays.asList(3, 1, 2));
        nat.sort(null);
        check(nat.equals(Arrays.asList(1, 2, 3)), "sort(null) natural ordering");
        List<Integer> filt = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        filt.removeIf(x -> x % 2 == 0);
        check(filt.equals(Arrays.asList(1, 3)), "removeIf");
        expectThrows(() -> new ArrayList<Integer>(Arrays.asList(1)).replaceAll(null),
                NullPointerException.class, "replaceAll(null) NPE");

        check(List.ofLazy(3, i -> i * i).equals(Arrays.asList(0, 1, 4)), "ofLazy");
        expectThrows(() -> List.ofLazy(-1, i -> i), IllegalArgumentException.class,
                "ofLazy negative size");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
