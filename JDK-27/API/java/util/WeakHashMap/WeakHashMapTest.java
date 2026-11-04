/*
 * @test
 * @summary java.util.WeakHashMap 一致性：白盒（反射结构）+ 黑盒（Map 契约 + null 语义）。
 * @run main WeakHashMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractMap、实现 Map、put/get/remove/视图成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：WeakHashMap 的 key 由弱引用持有——key 不再被外部强引用时可能被 GC 回收；
 *    除此之外是普通 Map：允许 null key/value，不保证迭代顺序。
 * 2. 场景：new WeakHashMap() -> put -> get/containsKey/containsValue -> remove ->
 *    keySet/values/entrySet 视图 -> equals/hashCode -> clear。
 * 3. 组合链路：插入多键 -> 校验 contains/size -> 视图 remove 联动 -> 清理。
 * 4. 不变量：size 精确；等值 map hashCode 相等；视图修改反映到 map。
 * 5. 边界：null key/value 允许；弱引用回收时机不确定，故不断言 gc 后的 get==null。
 */

import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.WeakHashMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "keySet") && hasMethod(c, "values") && hasMethod(c, "entrySet"),
              "keySet/values/entrySet views");
    }

    static void testBasicContract() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        check(m.isEmpty() && m.size() == 0, "empty");
        check(m.put("a", 1) == null, "put new returns null");
        check(m.put("a", 10) == 1, "put existing returns old");
        m.put("b", 2);
        m.put("c", 3);
        check(m.get("a") == 10 && m.get("b") == 2, "get");
        check(m.get("missing") == null, "get missing");
        check(m.containsKey("a") && m.containsKey("c"), "containsKey");
        check(m.containsValue(2) && !m.containsValue(99), "containsValue");
        check(m.remove("b") == 2 && !m.containsKey("b"), "remove");
        check(m.remove("missing") == null, "remove missing returns null");
        check(m.size() == 2, "size");
    }

    static void testNullSemantics() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put(null, 7);
        m.put("n", null);
        check(m.get(null) == 7, "null key retrievable");
        check(m.containsKey(null) && m.containsValue(null), "contains null key/value");
        check(m.get("n") == null && m.containsKey("n"), "null value present");
        m.remove(null);
        check(!m.containsKey(null), "remove null key");
    }

    static void testViewsAndEquals() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        check(m.keySet().size() == 3 && m.keySet().contains("a"), "keySet");
        check(m.values().size() == 3, "values");
        check(m.entrySet().size() == 3, "entrySet");
        check(m.keySet().remove("a") && !m.containsKey("a") && m.size() == 2,
              "keySet.remove reflects in map");
        check(m.values().remove(2) && !m.containsKey("b"), "values.remove reflects in map");

        WeakHashMap<String, Integer> copy = new WeakHashMap<>();
        copy.put("c", 3);
        copy.put("x", 4);
        WeakHashMap<String, Integer> same = new WeakHashMap<>();
        same.put("x", 4);
        same.put("c", 3);
        check(copy.equals(same) && copy.hashCode() == same.hashCode(),
              "equals/hashCode order-independent");
        check(!m.equals(copy), "!equals different content");

        for (Map.Entry<String, Integer> e : m.entrySet()) {
            check(m.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        m.clear();
        check(m.isEmpty(), "clear");
    }

    static void testBlackBox() {
        WeakHashMap<String, Integer> a = new WeakHashMap<>(16, 0.75f);
        a.put("x", 1);
        check(a.get("x") == 1, "ctor (int,float) works");
        WeakHashMap<String, Integer> b = new WeakHashMap<>(8);
        b.put("y", 2);
        check(b.get("y") == 2, "ctor (int) works");

        // copy constructor + putAll populate independent content
        WeakHashMap<String, Integer> src = new WeakHashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        WeakHashMap<String, Integer> copy = new WeakHashMap<>(src);
        check(copy.equals(src) && copy.size() == 2, "ctor (Map) copies");
        copy.put("c", 3);
        check(src.size() == 2, "ctor (Map) independent");

        WeakHashMap<String, Integer> dest = new WeakHashMap<>();
        dest.putAll(src);
        check(dest.equals(src) && dest.size() == 2, "putAll copies");

        // newWeakHashMap(int) static factory (JDK 19+)
        WeakHashMap<String, Integer> viaFactory = WeakHashMap.newWeakHashMap(8);
        viaFactory.put("z", 9);
        check(viaFactory.get("z") == 9, "newWeakHashMap(int) factory");

        // forEach visits every mapping
        final int[] count = {0};
        src.forEach((k, v) -> { count[0] += v; });
        check(count[0] == 3, "forEach sums values");

        // replaceAll transforms values in place
        src.replaceAll((k, v) -> v * 10);
        check(src.get("a") == 10 && src.get("b") == 20, "replaceAll transforms values");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasicContract();
        testNullSemantics();
        testViewsAndEquals();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
