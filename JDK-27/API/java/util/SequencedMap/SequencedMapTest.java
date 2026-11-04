/*
 * @test
 * @summary java.util.SequencedMap conformance: white-box structure + black-box behavior.
 * @run main SequencedMapTest
 */

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class SequencedMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedMap");
        check(c.isInterface(), "is an interface");
        check(Map.class.isAssignableFrom(c), "extends Map");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
        for (String m : new String[] {"firstEntry", "lastEntry", "pollFirstEntry",
                "pollLastEntry"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " default");
        for (String m : new String[] {"putFirst", "putLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m, Object.class, Object.class)
                    .getModifiers()), m + " default");
    }

    static void testBlackBox() {
        SequencedMap<String, Integer> m = new LinkedHashMap<>();
        m.putFirst("a", 1);
        m.putLast("c", 3);
        m.putFirst("z", 26);
        check(new java.util.ArrayList<>(m.keySet()).equals(java.util.List.of("z", "a", "c")),
                "putFirst/putLast order");
        check("z".equals(m.firstEntry().getKey()) && Integer.valueOf(26).equals(m.firstEntry().getValue()),
                "firstEntry");
        check("c".equals(m.lastEntry().getKey()), "lastEntry");
        check("z".equals(m.pollFirstEntry().getKey()) && "c".equals(m.pollLastEntry().getKey()),
                "pollFirst/pollLast");
        check(m.size() == 1 && m.firstEntry().getKey().equals("a"), "size after polls");
        check(m.reversed().firstEntry().getKey().equals("a"), "reversed view");
    }

    // ===================== 契约深化：putFirst/Last 既有键重定位/entry 快照 UOE/空返回 null/reversed 活视图/TreeMap 载体 =====================

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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

    static java.util.List<String> keys(SequencedMap<String, Integer> m) {
        return new java.util.ArrayList<>(m.keySet());
    }

    static void testPutFirstLastExistingKey() {
        // putFirst/putLast 对既有键：重定位 + 更新值（返回旧值）
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.putFirst("a", 1); m.putLast("c", 3); m.putFirst("z", 26);
        check(keys(m).equals(java.util.List.of("z", "a", "c")), "initial order");
        Integer old = m.putFirst("c", 33);
        check(old != null && old == 3, "putFirst(existing) returns old value");
        check(keys(m).equals(java.util.List.of("c", "z", "a")), "putFirst(existing) relocates to first");
        check(m.get("c") == 33, "putFirst(existing) updates value");
        m.putLast("z", 260);
        check(keys(m).equals(java.util.List.of("c", "a", "z")), "putLast(existing) relocates to last");
        check(m.get("z") == 260 && m.size() == 3, "value updated, no duplication");
        // 新键正常两端插入
        m.putFirst("head", 0);
        check(keys(m).get(0).equals("head"), "putFirst(new) at first");
        m.putLast("tail", 99);
        check(keys(m).get(keys(m).size() - 1).equals("tail"), "putLast(new) at last");
        // put 仍在尾端（LinkedHashMap 语义不变）
        m.put("mid", 50);
        check(keys(m).get(keys(m).size() - 1).equals("mid"), "put appends at last");
    }

    static void testEntrySnapshots() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1); m.put("b", 2);
        // firstEntry/lastEntry 返回不可变快照：setValue → UOE 且映射不变
        Map.Entry<String, Integer> fe = m.firstEntry();
        check("a".equals(fe.getKey()) && Integer.valueOf(1).equals(fe.getValue()), "firstEntry content");
        checkThrows(UnsupportedOperationException.class, () -> fe.setValue(999),
              "firstEntry().setValue UOE (immutable snapshot)");
        check(m.get("a") == 1, "map unchanged after failed snapshot setValue");
        Map.Entry<String, Integer> le = m.lastEntry();
        check("b".equals(le.getKey()), "lastEntry content");
        checkThrows(UnsupportedOperationException.class, () -> le.setValue(999),
              "lastEntry().setValue UOE");
        // pollFirstEntry/pollLastEntry：先删后返快照
        Map.Entry<String, Integer> pf = m.pollFirstEntry();
        check("a".equals(pf.getKey()) && Integer.valueOf(1).equals(pf.getValue()),
              "pollFirstEntry returns removed entry");
        check(!m.containsKey("a") && m.size() == 1, "pollFirstEntry removed");
        checkThrows(UnsupportedOperationException.class, () -> pf.setValue(1),
              "pollFirstEntry().setValue UOE (snapshot)");
        Map.Entry<String, Integer> pl = m.pollLastEntry();
        check("b".equals(pl.getKey()), "pollLastEntry returns removed entry");
        check(m.isEmpty(), "drained");
    }

    static void testEmptySemantics() {
        // 空映射：first/last/poll* 返回 null（不抛 NSEE——与 SequencedCollection 的 NSEE 相对）
        SequencedMap<String, Integer> m = new LinkedHashMap<>();
        check(m.firstEntry() == null, "empty firstEntry null");
        check(m.lastEntry() == null, "empty lastEntry null");
        check(m.pollFirstEntry() == null, "empty pollFirstEntry null");
        check(m.pollLastEntry() == null, "empty pollLastEntry null");
    }

    static void testReversedLiveView() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1); m.put("b", 2);
        SequencedMap<String, Integer> rev = m.reversed();
        check(rev.firstEntry().getKey().equals("b") && rev.lastEntry().getKey().equals("a"),
              "reversed endpoints");
        // Map.put 无位置语义：落在底层尾端（即原映射的末尾，不是首端——与 List.reversed().add 相对）
        rev.put("front", 0);
        check(keys(m).equals(java.util.List.of("a", "b", "front")),
              "reversed.put lands at original tail (put has no positional insert)");
        check(rev.firstEntry().getKey().equals("front"), "view first == original last");
        check(rev.size() == m.size(), "size linkage");
        // 视图 putFirst == 原映射 putLast
        rev.putFirst("back", 9);
        check(keys(m).get(keys(m).size() - 1).equals("back"), "view putFirst == original putLast");
        // 视图 pollFirstEntry 删原映射 last
        check("back".equals(rev.pollFirstEntry().getKey()), "view pollFirst removes original last");
        check(keys(m).equals(java.util.List.of("a", "b", "front")), "original after view poll");
        check(keys(rev.reversed()).equals(keys(m)), "reversed().reversed() restores order");
    }

    static void testTreeMapCarrier() {
        // TreeMap 实现 SequencedMap：读/删可用；putFirst/putLast 违反序 → UOE
        java.util.TreeMap<String, Integer> tm = new java.util.TreeMap<>();
        check(tm instanceof SequencedMap, "TreeMap is a SequencedMap");
        tm.put("b", 2); tm.put("a", 1); tm.put("c", 3);
        check(tm.firstEntry().getKey().equals("a") && tm.lastEntry().getKey().equals("c"),
              "TreeMap first/lastEntry sorted");
        check(tm.pollLastEntry().getKey().equals("c") && tm.size() == 2, "TreeMap pollLastEntry");
        check(tm.reversed().firstEntry().getKey().equals("b"), "TreeMap reversed view");
        checkThrows(UnsupportedOperationException.class, () -> tm.putFirst("z", 9),
              "TreeMap putFirst UOE (order constraint)");
        checkThrows(UnsupportedOperationException.class, () -> tm.putLast("z", 9),
              "TreeMap putLast UOE (order constraint)");
        check(tm.size() == 2, "size unchanged after failed puts");
        check(new java.util.TreeMap<String, Integer>().pollFirstEntry() == null,
              "empty TreeMap pollFirstEntry null");
    }

    static void testWhoImplements() {
        check(new LinkedHashMap<String, Integer>() instanceof SequencedMap, "LinkedHashMap implements");
        check(new java.util.TreeMap<String, Integer>() instanceof SequencedMap, "TreeMap implements");
        check(!(new java.util.HashMap<String, Integer>() instanceof SequencedMap),
              "HashMap does NOT implement (unordered)");
        check(!(Map.<String, Integer>of() instanceof SequencedMap), "Map.of does not implement");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPutFirstLastExistingKey();
        testEntrySnapshots();
        testEmptySemantics();
        testReversedLiveView();
        testTreeMapCarrier();
        testWhoImplements();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
