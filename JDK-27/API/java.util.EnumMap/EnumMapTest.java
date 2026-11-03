/*
 * @test
 * @summary java.util.EnumMap 一致性：白盒（反射结构）+ 黑盒（枚举声明序 + 构造器 + null 语义）。
 * @run main EnumMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractMap、实现 Cloneable/Serializable、
 *   put/get/remove/containsKey/containsValue/视图/clone 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：EnumMap 以单一枚举类型为 key 的有序 Map，键序固定为枚举声明序（ordinal）；
 *    key 不能为 null（put 抛 NPE），value 允许 null；底层用数组实现。
 * 2. 场景：new EnumMap(Color.class) -> put 乱序 -> keySet 按 ordinal 迭代 -> get/remove ->
 *    putIfAbsent/compute/merge -> EnumMap(EnumMap) 拷贝 -> EnumMap(Map) 拷贝 -> clone。
 * 3. 组合链路：乱序插入 3 个枚举键 -> 校验声明序迭代 -> 默认方法更新 -> 构造器拷贝/clone。
 * 4. 不变量：迭代顺序恒等于枚举声明序，与插入顺序无关；equals 与顺序无关。
 * 5. 边界/异常：put(null,...) 抛 NPE；get/remove/containsKey(null) 优雅返回 null/false；
 *    EnumMap(空 Map) 抛 IAE；null value 允许。
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class EnumMapTest {
    enum Color { RED, GREEN, BLUE }

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
        Class<?> c = Class.forName("java.util.EnumMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "containsKey", Object.class), "containsKey");
        check(hasMethod(c, "containsValue", Object.class), "containsValue");
        check(hasMethod(c, "keySet") && hasMethod(c, "values") && hasMethod(c, "entrySet"), "views");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testOrderAndOps() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        check(map.isEmpty() && map.size() == 0, "empty");
        check(map.put(Color.BLUE, "blue") == null, "put new returns null");
        check(map.put(Color.RED, "red") == null, "put new returns null");
        map.put(Color.GREEN, "green");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "keySet iterated in declaration order");
        check(new ArrayList<>(map.values()).equals(Arrays.asList("red", "green", "blue")),
              "values follow key order");
        check(map.get(Color.RED).equals("red") && map.get(Color.BLUE).equals("blue"), "get");
        check(map.put(Color.RED, "RED").equals("red"), "put existing returns old");
        check(map.get(Color.RED).equals("RED"), "put updates");
        check(map.size() == 3, "size");
        check(map.containsKey(Color.GREEN) && !map.containsKey("GREEN"), "containsKey (absent non-enum)");
        check(map.containsValue("green") && !map.containsValue("nope"), "containsValue");
        check(map.remove(Color.GREEN).equals("green") && !map.containsKey(Color.GREEN), "remove");
        check(map.remove(Color.GREEN) == null, "remove absent returns null");
    }

    static void testNullHandling() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        expectThrows(() -> map.put(null, "x"), NullPointerException.class, "put null key");
        check(map.get(null) == null, "get null key returns null");
        check(map.remove(null) == null, "remove null key returns null");
        check(!map.containsKey(null), "containsKey null returns false");
        map.put(Color.RED, null);
        check(map.containsKey(Color.RED) && map.get(Color.RED) == null, "null value allowed");
    }

    static void testDefaultMethods() {
        EnumMap<Color, Integer> map = new EnumMap<>(Color.class);
        map.put(Color.RED, 1);
        check(map.getOrDefault(Color.RED, 0) == 1, "getOrDefault present");
        check(map.getOrDefault(Color.GREEN, 7) == 7, "getOrDefault missing");
        check(map.putIfAbsent(Color.RED, 99) == 1, "putIfAbsent present returns existing");
        check(map.putIfAbsent(Color.BLUE, 3) == null && map.get(Color.BLUE) == 3, "putIfAbsent inserts");
        check(map.replace(Color.RED, 10) == 1 && map.get(Color.RED) == 10, "replace");
        check(map.compute(Color.GREEN, (k, v) -> v == null ? 2 : v + 1) == 2, "compute absent");
        check(map.merge(Color.GREEN, 3, Integer::sum) == 5, "merge");
        map.replaceAll((k, v) -> v * 10);
        check(map.get(Color.RED) == 100 && map.get(Color.BLUE) == 30, "replaceAll");
        final int[] sum = {0};
        map.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 100 + 50 + 30, "forEach sums values");
    }

    static void testConstructorsAndClone() {
        EnumMap<Color, String> src = new EnumMap<>(Color.class);
        src.put(Color.RED, "red");
        src.put(Color.BLUE, "blue");

        EnumMap<Color, String> copy = new EnumMap<>(src);
        check(copy.equals(src) && copy != src, "EnumMap(EnumMap) copies");
        check(new ArrayList<>(copy.keySet()).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "copy preserves declaration order");

        Map<Color, String> plain = new java.util.HashMap<>();
        plain.put(Color.BLUE, "blue");
        plain.put(Color.RED, "red");
        EnumMap<Color, String> fromMap = new EnumMap<>(plain);
        check(fromMap.equals(src), "EnumMap(Map) copies content");
        check(new ArrayList<>(fromMap.keySet()).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "fromMap iteration in declaration order");

        expectThrows(() -> new EnumMap<Color, String>(new java.util.HashMap<>()),
                     IllegalArgumentException.class, "EnumMap(empty Map) throws IAE");

        EnumMap<Color, String> cloned = copy.clone();
        check(cloned.equals(copy) && cloned != copy, "clone deep copies");
        cloned.put(Color.GREEN, "green");
        check(!copy.containsKey(Color.GREEN), "clone independent of original");
    }

    static void testViews() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        map.put(Color.RED, "red");
        map.put(Color.GREEN, "green");
        map.put(Color.BLUE, "blue");
        check(map.keySet().size() == 3 && map.entrySet().size() == 3 && map.values().size() == 3,
              "view sizes");
        check(map.keySet().remove(Color.GREEN) && !map.containsKey(Color.GREEN), "keySet.remove reflects");
        for (Map.Entry<Color, String> e : map.entrySet()) {
            check(map.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        map.clear();
        check(map.isEmpty(), "clear");
    }

    static void testBlackBox() {
        // putAll copies all mappings from any Map with matching key type
        EnumMap<Color, String> target = new EnumMap<>(Color.class);
        target.put(Color.RED, "old");
        Map<Color, String> src = new java.util.HashMap<>();
        src.put(Color.GREEN, "green");
        src.put(Color.BLUE, "blue");
        src.put(Color.RED, "red");
        target.putAll(src);
        check(target.size() == 3, "putAll copies all mappings");
        check(target.get(Color.RED).equals("red") && target.get(Color.GREEN).equals("green")
              && target.get(Color.BLUE).equals("blue"), "putAll overwrites and inserts");
        check(new ArrayList<>(target.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "putAll keeps declaration order");

        // hashCode follows the Map contract: equal maps have equal hashCode
        EnumMap<Color, String> a = new EnumMap<>(Color.class);
        a.put(Color.RED, "red");
        a.put(Color.GREEN, "green");
        a.put(Color.BLUE, "blue");
        EnumMap<Color, String> b = new EnumMap<>(Color.class);
        b.put(Color.BLUE, "blue");
        b.put(Color.GREEN, "green");
        b.put(Color.RED, "red");
        check(a.equals(b), "equal EnumMaps are equal");
        check(a.hashCode() == b.hashCode(), "equal EnumMaps have equal hashCode");
        b.put(Color.BLUE, "changed");
        check(!a.equals(b), "different content not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOrderAndOps();
        testNullHandling();
        testDefaultMethods();
        testConstructorsAndClone();
        testViews();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
