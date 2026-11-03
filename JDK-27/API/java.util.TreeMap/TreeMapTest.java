/*
 * @test
 * @summary java.util.TreeMap 一致性：白盒（反射结构）+ 黑盒（自然序/Comparator/NavigableMap）。
 * @run main TreeMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractMap、实现 NavigableMap/Cloneable/Serializable、
 *   firstKey/lastKey/ceilingKey/floorKey/higherKey/lowerKey/范围视图/descendingMap 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：TreeMap 是有序 NavigableMap——自然序或 Comparator；默认拒绝 null key（NPE），
 *    value 允许 null；提供 first/last/ceiling/floor/higher/lower 与 subMap/headMap/tailMap。
 * 2. 场景：new TreeMap() 自然序；new TreeMap(Comparator.reverseOrder()) 自定义序；
 *    put 乱序 -> firstKey/lastKey -> ceiling/floor/higher/lower -> 范围视图 -> 视图写入联动。
 * 3. 组合链路：插入 1..10 -> 查询上下界 -> subMap/headMap/tailMap 视图 -> descendingMap 倒序
 *    -> pollFirst/pollLast 修改 map。
 * 4. 不变量：迭代有序；范围视图与 map 联动；comparator() 与构造一致。
 * 5. 边界/异常：null key 抛 NPE；subMap(from,to) from>to 抛 IAE；视图越界写入抛 IAE。
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TreeMapTest {
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
        Class<?> c = Class.forName("java.util.TreeMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(NavigableMap.class.isAssignableFrom(c), "implements NavigableMap");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "comparator"), "comparator");
        check(hasMethod(c, "firstKey") && hasMethod(c, "lastKey"), "firstKey/lastKey");
        check(hasMethod(c, "ceilingKey", Object.class) && hasMethod(c, "floorKey", Object.class), "ceiling/floorKey");
        check(hasMethod(c, "higherKey", Object.class) && hasMethod(c, "lowerKey", Object.class), "higher/lowerKey");
        check(hasMethod(c, "subMap", Object.class, Object.class), "subMap");
        check(hasMethod(c, "headMap", Object.class) && hasMethod(c, "tailMap", Object.class), "headMap/tailMap");
        check(hasMethod(c, "descendingMap") && hasMethod(c, "descendingKeySet"), "descendingMap/KeySet");
        check(hasMethod(c, "pollFirstEntry") && hasMethod(c, "pollLastEntry"), "pollFirst/LastEntry");
    }

    static void testNaturalOrder() {
        TreeMap<String, Integer> map = new TreeMap<>();
        check(map.comparator() == null, "natural ordering comparator null");
        map.put("c", 3);
        map.put("a", 1);
        map.put("b", 2);
        check(map.firstKey().equals("a") && map.lastKey().equals("c"), "firstKey/lastKey");
        check(map.firstEntry().getValue() == 1 && map.lastEntry().getValue() == 3, "first/lastEntry");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "natural iteration order");
        check(map.get("b") == 2, "get");
        check(map.put("a", 10) == 1 && map.get("a") == 10, "put existing returns old");
        check(map.remove("b") == 2 && !map.containsKey("b"), "remove");
        check(map.size() == 2, "size");
        map.put("x", null);
        check(map.containsKey("x") && map.get("x") == null, "null value allowed");
    }

    static void testComparatorOrder() {
        TreeMap<Integer, String> map = new TreeMap<>(Comparator.reverseOrder());
        map.put(1, "one");
        map.put(3, "three");
        map.put(2, "two");
        check(map.firstKey() == 3 && map.lastKey() == 1, "reverse comparator first/last");
        check(map.comparator() == Comparator.<Integer>reverseOrder(), "comparator() reflects ctor");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(3, 2, 1)),
              "reverse iteration order");
    }

    static void testNavigable() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i += 2) {
            map.put(i, "v" + i);
        }
        check(map.lowerKey(5) == 3, "lowerKey");
        check(map.floorKey(5) == 5 && map.floorKey(4) == 3, "floorKey");
        check(map.ceilingKey(5) == 5 && map.ceilingKey(4) == 5, "ceilingKey");
        check(map.higherKey(5) == 7, "higherKey");
        check(map.lowerEntry(3).getValue().equals("v1"), "lowerEntry");
        check(map.ceilingEntry(8).getValue().equals("v9"), "ceilingEntry");
        check(map.higherEntry(9) == null, "higherEntry past max returns null");
        check(map.lowerEntry(1) == null, "lowerEntry before min returns null");

        Map.Entry<Integer, String> first = map.pollFirstEntry();
        check(first.getKey() == 1 && first.getValue().equals("v1"), "pollFirstEntry");
        check(map.pollLastEntry().getKey() == 9, "pollLastEntry");
        check(map.firstKey() == 3 && map.lastKey() == 7, "first/last after polls");
        check(map.size() == 3, "size after polls");
    }

    static void testRangeViewsAndLinkage() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            map.put(i, "v" + i);
        }
        check(map.subMap(3, 7).size() == 4, "subMap from inclusive, to exclusive");
        check(map.subMap(3, 7).firstKey() == 3 && map.subMap(3, 7).lastKey() == 6,
              "subMap endpoints");
        check(map.subMap(3, true, 7, true).size() == 5, "subMap inclusive both bounds");
        check(map.headMap(4).size() == 3, "headMap exclusive");
        check(map.headMap(4, true).size() == 4, "headMap inclusive");
        check(map.tailMap(8).size() == 3, "tailMap inclusive");
        check(map.tailMap(8, false).size() == 2, "tailMap exclusive");

        // 视图与 map 联动
        java.util.SortedMap<Integer, String> sub = map.subMap(3, 7);
        expectThrows(() -> sub.put(100, "out"), IllegalArgumentException.class,
                     "subMap put outside range throws IAE");
        sub.put(5, "five");
        check(map.get(5).equals("five"), "subMap put reflects in map");
        sub.remove(4);
        check(!map.containsKey(4), "subMap remove reflects in map");

        java.util.NavigableMap<Integer, String> desc = map.descendingMap();
        check(desc.firstKey() == 10 && desc.lastKey() == 1, "descendingMap first/last");

        expectThrows(() -> map.subMap(7, 3), IllegalArgumentException.class,
                     "subMap from>to throws IAE");
    }

    static void testNullKeyRejection() {
        TreeMap<String, Integer> natural = new TreeMap<>();
        expectThrows(() -> natural.put(null, 1), NullPointerException.class,
                     "natural-order put null key");
        TreeMap<Integer, Integer> cmp = new TreeMap<>(Comparator.naturalOrder());
        expectThrows(() -> cmp.put(null, 1), NullPointerException.class,
                     "comparator-order put null key");
        natural.put("k", null);
        check(natural.containsKey("k") && natural.get("k") == null, "null value allowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrder();
        testComparatorOrder();
        testNavigable();
        testRangeViewsAndLinkage();
        testNullKeyRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
