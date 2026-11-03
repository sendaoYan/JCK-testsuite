/*
 * @test
 * @summary java.util.HashMap 一致性：白盒（反射结构）+ 黑盒（完整 Map 契约链路）。
 * @run main HashMapTest
 *
 * 白盒（结构）：Class.forName 加载、非接口/枚举、继承 AbstractMap、实现 Map/Cloneable/
 *   Serializable，关键成员形态（put/get/remove/默认方法/视图）。
 * 黑盒（行为）设计案：
 * 1. 契约：HashMap 是允许 null key 与 null value 的哈希表；不保证迭代顺序；
 *    get/put 平均 O(1)，size 精确，equals/hashCode 与顺序无关。
 * 2. 场景：new HashMap() -> put -> get/containsKey/containsValue -> replace/replaceAll
 *    -> remove -> putIfAbsent/getOrDefault -> computeIfAbsent/computeIfPresent/compute/merge
 *    -> size/isEmpty -> keySet/values/entrySet 视图 -> equals/hashCode -> clear -> forEach。
 * 3. 组合链路：先在同一个 map 上串起来走完整 put→默认方法→视图→清理链路，再单独验证
 *    默认方法的 null 结果语义（compute/merge 返回 null 时删除条目）。
 * 4. 不变量：size 与 contains 始终一致；等值 map 的 hashCode 相等；视图修改直接反映到 map。
 * 5. 边界/异常：null key/value 均允许；视图迭代器结构修改后 fail-fast 抛
 *    ConcurrentModificationException；computeIfAbsent 返回 null 不插入。
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HashMapTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.HashMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        // 关键成员形态
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "putIfAbsent", Object.class, Object.class), "putIfAbsent");
        check(hasMethod(c, "getOrDefault", Object.class, Object.class), "getOrDefault");
        check(hasMethod(c, "computeIfAbsent", Object.class, Function.class), "computeIfAbsent");
        check(hasMethod(c, "compute", Object.class, BiFunction.class), "compute");
        check(hasMethod(c, "merge", Object.class, Object.class, BiFunction.class), "merge");
        check(hasMethod(c, "replaceAll", BiFunction.class), "replaceAll");
        check(hasMethod(c, "forEach", BiConsumer.class), "forEach");
        check(hasMethod(c, "keySet") && hasMethod(c, "values") && hasMethod(c, "entrySet"),
              "keySet/values/entrySet views");
    }

    static void testContractChain() {
        HashMap<String, Integer> map = new HashMap<>();

        check(map.put("a", 1) == null, "put new returns null");
        check(map.put("a", 2) == 1, "put existing returns old value");
        check(map.put("b", 3) == null && map.put("c", 4) == null, "put more");
        check(map.get("a") == 2 && map.get("missing") == null, "get");
        check(map.containsKey("a") && !map.containsKey("z"), "containsKey");
        check(map.containsValue(3) && !map.containsValue(99), "containsValue");

        check(map.replace("a", 20) == 2, "replace(K,V) returns old");
        check(map.replace("a", 20, 200), "replace(K,old,new) matching");
        check(!map.replace("a", 999, 1), "replace(K,old,new) non-matching false");
        check(map.get("a") == 200, "replace updated");
        check(map.replace("absent", 5) == null, "replace absent returns null");
        map.replaceAll((k, v) -> v * 10);
        check(map.get("a") == 2000 && map.get("b") == 30 && map.get("c") == 40, "replaceAll");

        check(map.remove("b") == 30, "remove returns value");
        check(map.remove("b") == null, "remove absent returns null");
        check(!map.remove("a", 1), "remove(K,V) non-matching false");
        check(map.remove("a", 2000), "remove(K,V) matching true");
        check(!map.containsKey("a") && !map.containsKey("b"), "removed absent");

        check(map.putIfAbsent("c", 9) == 40, "putIfAbsent present returns existing");
        check(map.putIfAbsent("d", 8) == null, "putIfAbsent absent returns null");
        check(map.get("d") == 8, "putIfAbsent inserted");
        check(map.getOrDefault("d", 0) == 8, "getOrDefault present");
        check(map.getOrDefault("z", 0) == 0, "getOrDefault missing default");
        map.put("nullVal", null);
        check(map.getOrDefault("nullVal", 42) == null, "getOrDefault present-but-null returns null");

        check(map.computeIfAbsent("e", k -> 5) == 5, "computeIfAbsent computes");
        check(map.computeIfAbsent("e", k -> 99) == 5, "computeIfAbsent present no-op");
        check(map.computeIfAbsent("f", k -> null) == null && !map.containsKey("f"),
              "computeIfAbsent null result does not insert");
        check(map.computeIfPresent("e", (k, v) -> v * 2) == 10, "computeIfPresent computes");
        check(map.computeIfPresent("z", (k, v) -> 1) == null, "computeIfPresent absent no-op");
        check(map.computeIfPresent("nullVal", (k, v) -> 7) == null, "computeIfPresent null-value no-op");
        check(map.compute("e", (k, v) -> v + 1) == 11, "compute computes");
        check(map.compute("g", (k, v) -> v == null ? 3 : v) == 3, "compute on absent with null old");
        map.put("gone", 1);
        check(map.compute("gone", (k, v) -> null) == null && !map.containsKey("gone"),
              "compute null result removes");
        check(map.merge("e", 100, Integer::sum) == 111, "merge combines");
        check(map.merge("h", 6, Integer::sum) == 6, "merge absent inserts");
        check(map.merge("h", 4, Integer::sum) == 10, "merge combines existing");
        map.put("rm", 1);
        check(map.merge("rm", 2, (a, b) -> null) == null && !map.containsKey("rm"),
              "merge null result removes");

        // 最终剩余 c,d,nullVal,e,g,h 共 6 个条目
        check(map.size() == 6 && !map.isEmpty(), "size/isEmpty non-empty");
        check(map.containsKey("c") && map.containsKey("d") && map.containsKey("nullVal")
              && map.containsKey("e") && map.containsKey("g") && map.containsKey("h"),
              "remaining keys present");
    }

    static void testNullSemantics() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(null, 1);
        map.put("n", null);
        check(map.get(null) == 1, "null key retrievable");
        check(map.get("n") == null && map.containsKey("n"), "null value present");
        check(map.containsKey(null) && map.containsValue(null), "contains null key/value");
        map.remove(null);
        check(!map.containsKey(null), "remove null key");
    }

    static void testViewsAndFailFast() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Set<String> keys = map.keySet();
        check(keys.size() == 3 && keys.containsAll(java.util.Arrays.asList("a", "b", "c")), "keySet");
        check(map.values().size() == 3, "values size");
        check(map.entrySet().size() == 3, "entrySet size");

        check(keys.remove("a") && !map.containsKey("a") && map.size() == 2, "keySet.remove reflects");
        check(map.values().remove(2) && !map.containsKey("b"), "values.remove reflects");
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            check(map.get(e.getKey()).equals(e.getValue()), "entrySet entry matches map");
        }

        HashMap<String, Integer> m2 = new HashMap<>();
        m2.put("a", 1);
        Iterator<String> it = m2.keySet().iterator();
        m2.put("b", 2);
        expectThrows(it::next, java.util.ConcurrentModificationException.class,
                     "keySet iterator fail-fast");

        HashMap<String, Integer> m3 = new HashMap<>();
        m3.put("a", 1);
        Iterator<Map.Entry<String, Integer>> eit = m3.entrySet().iterator();
        m3.put("b", 2);
        expectThrows(eit::next, java.util.ConcurrentModificationException.class,
                     "entrySet iterator fail-fast");
    }

    static void testEqualsHashCodeClearForEach() {
        HashMap<String, Integer> a = new HashMap<>();
        a.put("x", 1);
        a.put("y", 2);
        HashMap<String, Integer> b = new HashMap<>();
        b.put("y", 2);
        b.put("x", 1);
        check(a.equals(b) && b.equals(a), "equals order-independent");
        check(a.hashCode() == b.hashCode(), "equal maps equal hashCode");
        b.put("z", 3);
        check(!a.equals(b), "!equals different content");

        final int[] sum = {0};
        a.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 3, "forEach sums values");

        a.clear();
        check(a.isEmpty() && a.size() == 0, "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testContractChain();
        testNullSemantics();
        testViewsAndFailFast();
        testEqualsHashCodeClearForEach();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
