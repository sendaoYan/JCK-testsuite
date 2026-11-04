/*
 * @test
 * @summary java.util.List conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main ListTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
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


    // ---- deepened II: of/copyOf 全矩阵 / hashCode 公式 / equals 契约 /
    //      ofLazy 惰性与缓存 / Arrays.asList 视图 / 越界矩阵 / 排序稳定性 ---------

    static void testOfFactoryMatrix() {
        // iteration order = argument order; equals/hashCode List contract
        List<String> l = List.of("a", "b");
        StringBuilder sb = new StringBuilder();
        for (String s : l) sb.append(s);
        check(sb.toString().equals("ab"), "of iteration order = argument order");
        check(l.hashCode() == 4066, "of [a,b] hashCode 4066 (List formula)");
        check(List.of().hashCode() == 1, "of() hashCode 1");
        check(List.of("a").hashCode() == 128, "of [a] hashCode 128");
        check(l.equals(new ArrayList<>(Arrays.asList("a", "b")))
              && new ArrayList<>(Arrays.asList("a", "b")).equals(l), "of equals ArrayList both ways");
        check(l.equals(new java.util.LinkedList<>(Arrays.asList("a", "b"))), "of equals LinkedList");
        // immutability: every mutation channel UOE
        expectThrows(() -> l.add("x"), UnsupportedOperationException.class, "of add UOE");
        expectThrows(() -> l.add(0, "x"), UnsupportedOperationException.class, "of add(int) UOE");
        expectThrows(() -> l.set(0, "x"), UnsupportedOperationException.class, "of set UOE");
        expectThrows(() -> l.remove(0), UnsupportedOperationException.class, "of remove(int) UOE");
        expectThrows(() -> l.remove("a"), UnsupportedOperationException.class, "of remove(Object) UOE");
        expectThrows(() -> l.clear(), UnsupportedOperationException.class, "of clear UOE");
        expectThrows(() -> l.replaceAll(x -> x), UnsupportedOperationException.class, "of replaceAll UOE");
        expectThrows(() -> l.sort(Comparator.naturalOrder()), UnsupportedOperationException.class, "of sort UOE");
        expectThrows(() -> l.removeIf(x -> true), UnsupportedOperationException.class, "of removeIf UOE");
        check(l.equals(List.of("a", "b")), "of unchanged after failed mutations");
        // null rejection in all arities
        expectThrows(() -> List.of((String) null), NullPointerException.class, "of(e) null NPE");
        expectThrows(() -> List.of("a", "b", (String) null), NullPointerException.class, "of 3-arg null NPE");
        expectThrows(() -> List.of((String[]) null), NullPointerException.class, "of(array null) NPE");
        // single-element factory
        List<String> single = List.of("only");
        check(single.size() == 1 && single.get(0).equals("only"), "single element of");
        // List.of implements RandomAccess
        check(l instanceof java.util.RandomAccess, "List.of is RandomAccess");
        // subList of immutable list is immutable
        expectThrows(() -> l.subList(0, 1).add("x"), UnsupportedOperationException.class,
                "of subList add UOE");
        check(l.subList(0, 1).equals(List.of("a")), "of subList content");
        // empty list operations safe
        check(List.of().contains("a") == false && List.of().indexOf("a") == -1, "of() queries");
        expectThrows(() -> List.of().get(0), IndexOutOfBoundsException.class, "of() get(0) IOOBE");
    }

    static void testCopyOfContract() {
        List<String> src = new ArrayList<>(Arrays.asList("z"));
        List<String> copy = List.copyOf(src);
        src.add("w");
        check(copy.size() == 1 && copy.get(0).equals("z"), "copyOf snapshots source content");
        expectThrows(() -> copy.add("x"), UnsupportedOperationException.class, "copyOf immutable");
        check(List.copyOf(Arrays.asList()).isEmpty(), "copyOf of empty");
        expectThrows(() -> List.copyOf(null), NullPointerException.class, "copyOf(null) NPE");
        expectThrows(() -> List.copyOf(Arrays.asList("a", null)), NullPointerException.class,
                "copyOf null element NPE");
        // copyOf of a List.of may return the same instance (identity optimization is legal)
        List<String> imm = List.of("q");
        check(List.copyOf(imm).equals(imm), "copyOf of immutable equals source");
        // from a subList view
        List<Integer> view = new ArrayList<>(Arrays.asList(1, 2, 3)).subList(1, 3);
        check(List.copyOf(view).equals(List.of(2, 3)), "copyOf of subList");
    }

    static void testOfLazyContract() {
        int[] calls = {0};
        List<String> lz = List.ofLazy(2, i -> { calls[0]++; return "v" + i; });
        check(lz.size() == 2, "ofLazy size without invoking function");
        check(calls[0] == 0, "ofLazy lazy: size() did not call generator");
        check(lz.get(0).equals("v0") && lz.get(1).equals("v1"), "ofLazy generated elements");
        check(calls[0] == 2, "ofLazy get(i) invoked generator once per index");
        lz.get(0);
        check(calls[0] == 2, "ofLazy caches computed elements (no re-invocation)");
        StringBuilder sb = new StringBuilder();
        for (String s : lz) sb.append(s);
        check(sb.toString().equals("v0v1"), "ofLazy iteration");
        expectThrows(() -> lz.add("x"), UnsupportedOperationException.class, "ofLazy immutable add");
        expectThrows(() -> lz.set(0, "x"), UnsupportedOperationException.class, "ofLazy immutable set");
        expectThrows(() -> List.ofLazy(1, null), NullPointerException.class, "ofLazy null fn NPE");
        expectThrows(() -> List.ofLazy(-1, i -> "x"), IllegalArgumentException.class, "ofLazy(-1) IAE");
        check(List.ofLazy(0, i -> "x").isEmpty(), "ofLazy(0) empty");
        check(lz.hashCode() == Arrays.asList("v0", "v1").hashCode(), "ofLazy hashCode List formula");
        check(lz instanceof java.util.RandomAccess, "ofLazy is RandomAccess");
    }

    static void testHashCodeEqualsContract() {
        check(new ArrayList<String>().hashCode() == 1, "empty ArrayList hashCode 1");
        check(Arrays.asList("a", "b").hashCode() == 4066, "[a,b] hashCode 4066");
        check(Arrays.asList("a", "b", null).hashCode() == 126046, "[a,b,null] hashCode 126046");
        List<String> al = new ArrayList<>(Arrays.asList("a", "b"));
        check(al.equals(new java.util.Vector<>(Arrays.asList("a", "b"))), "cross-impl equals");
        check(!Arrays.asList("a", null).equals(Arrays.asList("a", "b")), "null vs value");
    }

    static void testSortStabilityAndChains() {
        List<String> st = new ArrayList<>(Arrays.asList("b2", "a1", "b1", "a2"));
        st.sort(Comparator.comparing(x -> x.substring(0, 1)));
        check(st.equals(Arrays.asList("a1", "a2", "b2", "b1")), "sort stable for equal keys");
        List<Integer> nul = new ArrayList<>(Arrays.asList(3, null, 1));
        nul.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        check(nul.equals(Arrays.asList(null, 1, 3)), "sort nullsFirst");
        List<String> s = new ArrayList<>(Arrays.asList("bb", "a", "ccc"));
        s.sort(Comparator.comparing(String::length).reversed());
        check(s.equals(Arrays.asList("ccc", "bb", "a")), "sort reversed comparator");
    }

    static void testArraysAsListView() {
        List<String> v = Arrays.asList("a", "b");
        check(v.set(0, "z").equals("a"), "asList set returns old");
        expectThrows(() -> v.add("x"), UnsupportedOperationException.class, "asList add UOE");
        expectThrows(() -> v.remove(0), UnsupportedOperationException.class, "asList remove UOE");
        expectThrows(() -> v.clear(), UnsupportedOperationException.class, "asList clear UOE");
        List<String> sub = v.subList(0, 1);
        expectThrows(() -> sub.add("x"), UnsupportedOperationException.class, "asList subList add UOE");
        check(sub.set(0, "y").equals("z"), "asList subList set passes through");
        check(v.equals(new ArrayList<>(Arrays.asList("y", "b"))), "asList equals ArrayList");
    }

    static void testIndexedBoundContract() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        expectThrows(() -> l.get(-1), IndexOutOfBoundsException.class, "get(-1) IOOBE");
        expectThrows(() -> l.get(2), IndexOutOfBoundsException.class, "get(size) IOOBE");
        expectThrows(() -> l.set(2, "x"), IndexOutOfBoundsException.class, "set(size) IOOBE");
        expectThrows(() -> l.add(3, "x"), IndexOutOfBoundsException.class, "add(size+1) IOOBE");
        expectThrows(() -> l.remove(2), IndexOutOfBoundsException.class, "remove(size) IOOBE");
        expectThrows(() -> l.addAll(3, Arrays.asList("c")), IndexOutOfBoundsException.class,
                "addAll(size+1) IOOBE");
        expectThrows(() -> l.listIterator(3), IndexOutOfBoundsException.class, "listIterator(size+1) IOOBE");
        check(l.equals(Arrays.asList("a", "b")), "failed ops leave list intact");
        l.add(2, "c");
        check(l.equals(Arrays.asList("a", "b", "c")), "add(size,e) appends");
    }

    static void testSubListViewContract() {
        List<Integer> l = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = l.subList(1, 4);
        check(sub.hashCode() == Arrays.asList(2, 3, 4).hashCode(), "subList hashCode");
        sub.set(0, 20);
        check(l.get(1) == 20, "subList set passes through");
        sub.add(0, 99);
        check(l.equals(Arrays.asList(1, 99, 20, 3, 4, 5)), "subList add passes through");
        sub.clear();
        check(l.equals(Arrays.asList(1, 5)), "subList clear removes range");
        check(l.subList(0, l.size()).equals(l), "full-range subList equals whole");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOfFactoryMatrix();
        testCopyOfContract();
        testOfLazyContract();
        testHashCodeEqualsContract();
        testSortStabilityAndChains();
        testArraysAsListView();
        testIndexedBoundContract();
        testSubListViewContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
