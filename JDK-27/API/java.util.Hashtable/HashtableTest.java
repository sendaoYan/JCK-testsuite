/*
 * @test
 * @summary java.util.Hashtable 一致性：白盒（反射结构）+ 黑盒（null 拒绝 + Enumeration）。
 * @run main HashtableTest
 *
 * 白盒（结构）：Class.forName 加载、继承 Dictionary、实现 Map/Cloneable/Serializable、
 *   put/get/remove/contains/containsKey/containsValue/keys/elements/默认方法成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：Hashtable 是线程安全哈希表（方法 synchronized），不允许 null key 也不允许
 *    null value（put/get/containsKey/contains/remove 以及被重写的默认方法均抛 NPE）。
 * 2. 场景：new Hashtable() -> put/get/remove -> contains/containsValue -> Enumeration keys/
 *    elements -> putIfAbsent/getOrDefault/replace/compute/merge -> 视图 -> equals/hashCode。
 * 3. 组合链路：先正常插入验证契约，再逐一触发 null 参数异常，最后验证 Enumeration 与视图。
 * 4. 不变量：contains(Object) 是 containsValue 的别名；equals/hashCode 与顺序无关。
 * 5. 边界/异常：接受 key/value 的方法对 null 抛 NPE；compute/merge 返回 null 时删除/不插入条目。
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.BiFunction;

public class HashtableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Hashtable");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.Dictionary"), "extends Dictionary");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "contains", Object.class), "contains(Object)");
        check(hasMethod(c, "containsKey", Object.class), "containsKey(Object)");
        check(hasMethod(c, "containsValue", Object.class), "containsValue(Object)");
        check(hasMethod(c, "keys") && hasMethod(c, "elements"), "keys/elements (Enumeration)");
        check(hasMethod(c, "putIfAbsent", Object.class, Object.class), "putIfAbsent");
        check(hasMethod(c, "compute", Object.class, BiFunction.class), "compute");
        check(hasMethod(c, "merge", Object.class, Object.class, BiFunction.class), "merge");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testBasicContract() {
        Hashtable<String, Integer> t = new Hashtable<>();
        check(t.isEmpty() && t.size() == 0, "empty");
        check(t.put("a", 1) == null, "put new returns null");
        check(t.put("a", 2) == 1, "put existing returns old");
        check(t.put("b", 3) == null, "put another");
        check(t.get("a") == 2 && t.get("missing") == null, "get");
        check(t.remove("a") == 2 && !t.containsKey("a"), "remove");
        check(t.remove("a") == null, "remove absent returns null");
        check(t.size() == 1, "size");
    }

    static void testNullRejection() {
        Hashtable<String, Integer> t = new Hashtable<>();
        expectNPE(() -> t.put(null, 1), "put null key");
        expectNPE(() -> t.put("k", null), "put null value");
        expectNPE(() -> t.get(null), "get null");
        expectNPE(() -> t.containsKey(null), "containsKey null");
        expectNPE(() -> t.contains(null), "contains null");
        expectNPE(() -> t.containsValue(null), "containsValue null");
        expectNPE(() -> t.remove(null), "remove null");
        expectNPE(() -> t.remove("k", null), "remove(K,V) null value");
        expectNPE(() -> t.replace(null, 1), "replace null key");
        expectNPE(() -> t.replace("k", null), "replace(K,V) null value");
        expectNPE(() -> t.putIfAbsent(null, 1), "putIfAbsent null key");
        expectNPE(() -> t.putIfAbsent("k", null), "putIfAbsent null value");
        expectNPE(() -> t.getOrDefault(null, 1), "getOrDefault null key");
        expectNPE(() -> t.computeIfAbsent(null, k -> 1), "computeIfAbsent null key");
        expectNPE(() -> t.computeIfPresent(null, (k, v) -> 1), "computeIfPresent null key");
        expectNPE(() -> t.compute(null, (k, v) -> 1), "compute null key");
        expectNPE(() -> t.merge(null, 1, (a, b) -> a), "merge null key");
    }

    static void testContainsAndEnumeration() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.containsKey("a") && !t.containsKey("z"), "containsKey");
        check(t.containsValue(1) && !t.containsValue(99), "containsValue");
        check(t.contains(1) && t.contains(2), "contains aliases containsValue");
        check(!t.contains("a"), "contains('a') false because 'a' is key not value");

        int keyCount = 0, valCount = 0;
        for (Enumeration<String> e = t.keys(); e.hasMoreElements(); ) {
            String k = e.nextElement();
            check(t.containsKey(k), "enumeration key present");
            keyCount++;
        }
        for (Enumeration<Integer> e = t.elements(); e.hasMoreElements(); ) {
            check(t.containsValue(e.nextElement()), "enumeration value present");
            valCount++;
        }
        check(keyCount == 2 && valCount == 2, "enumeration counts");
    }

    static void testDefaultMethodsAndViews() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        check(t.getOrDefault("a", 0) == 1, "getOrDefault present");
        check(t.getOrDefault("z", 9) == 9, "getOrDefault missing");
        check(t.putIfAbsent("a", 5) == 1, "putIfAbsent present returns existing");
        check(t.putIfAbsent("n", 6) == null && t.get("n") == 6, "putIfAbsent absent inserts");
        check(t.replace("a", 10) == 1 && t.get("a") == 10, "replace(K,V)");
        check(t.replace("a", 10, 11) && t.get("a") == 11, "replace(K,old,new)");
        t.replaceAll((k, v) -> v * 2);
        check(t.get("a") == 22, "replaceAll");
        check(t.compute("n", (k, v) -> v + 1) == 13, "compute");
        check(t.merge("n", 3, Integer::sum) == 16, "merge");
        // 返回 null 的 compute/merge 语义：删除条目或保持不存在（不抛异常）
        t.put("tmp", 1);
        check(t.compute("tmp", (k, v) -> null) == null && !t.containsKey("tmp"),
              "compute null result removes");
        check(t.computeIfAbsent("fresh", k -> null) == null && !t.containsKey("fresh"),
              "computeIfAbsent null result does not insert");
        t.put("mrg", 1);
        check(t.merge("mrg", 2, (a, b) -> null) == null && !t.containsKey("mrg"),
              "merge null result removes");

        final int[] sum = {0};
        t.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 22 + 16, "forEach");

        check(t.keySet().size() == 2, "keySet");
        check(t.values().size() == 2, "values");
        check(t.entrySet().size() == 2, "entrySet");
        check(t.keySet().remove("n") && !t.containsKey("n"), "keySet.remove reflects");
        for (Map.Entry<String, Integer> e : t.entrySet()) {
            check(t.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        t.clear();
        check(t.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        Hashtable<String, Integer> a = new Hashtable<>();
        a.put("x", 1);
        a.put("y", 2);
        Hashtable<String, Integer> b = new Hashtable<>();
        b.put("y", 2);
        b.put("x", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode order-independent");
        b.put("z", 3);
        check(!a.equals(b), "!equals different content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasicContract();
        testNullRejection();
        testContainsAndEnumeration();
        testDefaultMethodsAndViews();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
