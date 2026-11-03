/*
 * @test
 * @summary java.util.LinkedHashMap 一致性：白盒（反射结构）+ 黑盒（插入序/访问序 LRU）。
 * @run main LinkedHashMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 HashMap、实现 SequencedMap、
 *   putFirst/putLast/pollFirstEntry/pollLastEntry 与受保护 removeEldestEntry 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：LinkedHashMap 维护可预测迭代顺序——默认插入序，accessOrder=true 时访问序
 *    （get/put 更新都算访问，把条目移到末尾）；支持 removeEldestEntry 实现 LRU 淘汰；
 *    允许 null key/value。
 * 2. 场景：new LinkedHashMap() 插入序；new LinkedHashMap(16,.75f,true) 访问序；
 *    匿名子类覆盖 removeEldestEntry 做容量淘汰；SequencedMap 的 putFirst/putLast/firstEntry/
 *    lastEntry/pollFirstEntry/reversed。
 * 3. 组合链路：先插 3 个条目验证插入序 -> get/put 触发访问序重排 -> poll 移除首尾 ->
 *    reversed 视图倒序 -> removeEldestEntry 淘汰最老。
 * 4. 不变量：插入序下重复 put 不改变位置；访问序下访问过的条目在末尾；淘汰后 size 受限。
 * 5. 边界：null key/value 允许；重复 put 更新值但不移动（插入序）；访问序下 put 更新算访问。
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class LinkedHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasDeclared(Class<?> c, String name, Class<?>... p) {
        try { c.getDeclaredMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.LinkedHashMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.HashMap"), "extends HashMap");
        check(SequencedMap.class.isAssignableFrom(c), "implements SequencedMap");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(hasMethod(c, "putFirst", Object.class, Object.class), "putFirst");
        check(hasMethod(c, "putLast", Object.class, Object.class), "putLast");
        check(hasMethod(c, "pollFirstEntry") && hasMethod(c, "pollLastEntry"), "pollFirst/LastEntry");
        check(hasDeclared(c, "removeEldestEntry", Map.Entry.class), "removeEldestEntry declared");
    }

    static void testInsertionOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("a", 10);
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "insertion order preserved after re-put");
        check(map.get("a") == 10, "value updated");
        map.get("b");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "get does not reorder in insertion mode");
        map.put(null, 100);
        map.put("n", null);
        check(map.get(null) == 100 && map.get("n") == null, "null key/value allowed");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c", null, "n")),
              "null key inserted in position");
        map.remove("b");
        check(!map.containsKey("b") && map.size() == 4, "remove");
    }

    static void testAccessOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.get("a");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("b", "c", "a")),
              "access order moves get() entry to end");
        map.get("b");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("c", "a", "b")),
              "access order continues to reorder");
        map.put("c", 30);
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "put update counts as access");
        map.put("d", 4);
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c", "d")),
              "new key appended at end");
    }

    static void testRemoveEldestEntry() {
        class LRU extends LinkedHashMap<Integer, String> {
            LRU() { super(16, 0.75f, true); }
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 2;
            }
        }
        LRU lru = new LRU();
        lru.put(1, "one");
        lru.put(2, "two");
        lru.get(1);
        lru.put(3, "three");
        check(lru.size() == 2 && !lru.containsKey(2), "removeEldestEntry evicts eldest (2)");
        check(lru.containsKey(1) && lru.containsKey(3), "recent entries retained");
        lru.get(3);
        lru.put(4, "four");
        check(lru.size() == 2 && !lru.containsKey(1) && lru.containsKey(4),
              "access-order eviction evicts true eldest");
    }

    static void testSequencedMapOps() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("b", 2);
        map.put("c", 3);
        map.putFirst("a", 1);
        map.putLast("d", 4);
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c", "d")),
              "putFirst/putLast position");
        check(map.firstEntry().getKey().equals("a") && map.firstEntry().getValue() == 1, "firstEntry");
        check(map.lastEntry().getKey().equals("d") && map.lastEntry().getValue() == 4, "lastEntry");
        Map.Entry<String, Integer> first = map.pollFirstEntry();
        check(first.getKey().equals("a") && first.getValue() == 1 && map.size() == 3,
              "pollFirstEntry removes and returns first");
        check(map.pollLastEntry().getKey().equals("d"), "pollLastEntry returns last");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("b", "c")), "remaining order");
        check(new ArrayList<>(map.reversed().keySet()).equals(Arrays.asList("c", "b")),
              "reversed() view order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testInsertionOrder();
        testAccessOrder();
        testRemoveEldestEntry();
        testSequencedMapOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
