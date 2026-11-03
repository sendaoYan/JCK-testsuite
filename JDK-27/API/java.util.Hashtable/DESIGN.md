# java.util.Hashtable 测试方案

## 1. API 功能（按方法分组）

- 构造器：`Hashtable()`、`Hashtable(int initialCapacity)`、`Hashtable(int, float)`、`Hashtable(Map)`。
- 核心操作：`put`/`get`/`remove`/`remove(K,V)`/`clear`/`putAll`（均 synchronized）。
- 查询：`size`/`isEmpty`/`containsKey`/`containsValue`/`contains`（`contains` 是 `containsValue` 别名）。
- 枚举：`keys()`（Enumeration<K>）、`elements()`（Enumeration<V>）。
- 视图：`keySet`/`values`/`entrySet`。
- 重写的默认方法：`getOrDefault`/`putIfAbsent`/`replace`/`replaceAll`/`compute*`/`merge`/`forEach`（synchronized 且拒绝 null value）。
- 其它：`clone`、`toString`、`equals`/`hashCode`。

## 2. Java 用户如何使用

- 构造：`new Hashtable<>()`（线程安全但一般已被 `ConcurrentHashMap` 取代）；`Properties` 是其子类。
- 典型调用：`put`/`get`/`contains`/`containsValue`；`keys()`/`elements()` 枚举遍历。
- 真实组合链路：
  1. put/get/remove 基本契约。
  2. 逐个触发 null key/value 参数 → NPE（含被重写的默认方法）。
  3. contains vs containsValue 语义 + Enumeration 遍历。
  4. putIfAbsent/getOrDefault/replace/compute/merge → 返回 null 时删除/不插入（不抛 NPE，区别于 null 参数）。
  5. 视图联动 + equals/hashCode。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/Hashtable/SerializationDeadlock.java:49-50`：`new Hashtable<>()` 验证序列化并发不卡死。
- JDK 测试 `test/jdk/java/util/Hashtable/IllegalLoadFactor.java:46/57`：`new Hashtable(100, -3)`、`new Hashtable(100, Float.NaN)` 验证非法 loadFactor 抛 IAE。
- `java.util.Properties extends Hashtable<Object,Object>`：JDK 配置存储直接复用 Hashtable 的 put/get/keys/elements（来源 `java.util.Properties` 声明）。

## 4. 测试用例设计 + 自动化实现

实现位置：`HashtableTest.java`（`testWhiteBox()` + `testBasicContract()`/`testNullRejection()`/`testContainsAndEnumeration()`/`testDefaultMethodsAndViews()`/`testEqualsHashCode()`）。

- 正常：put/get/remove/contains/containsValue/Enumeration。
- 组合：默认方法（putIfAbsent/getOrDefault/replace/replaceAll/compute/merge）+ 返回 null 的删除/不插入语义。
- 边界/异常：null key 与 null value 参数一律 NPE（含默认方法重写）。
- 健壮性：确定性；无全局副作用；无文件/网络；跨平台。headless-safe。
