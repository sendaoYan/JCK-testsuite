# java.util.IdentityHashMap 测试方案

## 1. API 功能（按方法分组）

- 构造器：`IdentityHashMap()`、`IdentityHashMap(int expectedMaxSize)`、`IdentityHashMap(Map)`。
- 核心操作：`put`/`get`/`remove`/`remove(K,V)`/`clear`。
- 查询：`size`/`isEmpty`/`containsKey`/`containsValue`。
- 视图：`keySet`/`values`/`entrySet`。
- 契约方法：`equals`/`hashCode`（按引用相等语义）。

## 2. Java 用户如何使用

- 构造：`new IdentityHashMap<>()`，用于需要**引用相等（==）**而非 `equals` 的场景（如跟踪对象身份、序列化对象替换、按身份去重）。
- 典型调用：`put(k1, v)` 后 `get(k1)` 按引用命中；`get(new String("x"))` 与已存 `new String("x")` 不同。
- 真实组合链路：
  1. 放入两个内容相等但引用不同的 key → 两条独立映射。
  2. get/containsKey/remove 按引用命中；containsValue/remove(K,V) 也按引用比较 value。
  3. null key 允许；视图联动；equals 按引用语义比较 key 与 value。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 测试 `test/jdk/java/util/concurrent/PriorityBlockingQueue/WhiteBox.java:134/175`：
  `Collections.newSetFromMap(new IdentityHashMap())` —— 用 IdentityHashMap 作“按身份相等”的 Set 底层。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentHashMap/MapCheck.java:281`：
  `IdentityHashMap seen = new IdentityHashMap(size)` —— 用身份集合追踪已见对象。

## 4. 测试用例设计 + 自动化实现

实现位置：`IdentityHashMapTest.java`（`testWhiteBox()` + `testKeyIdentity()`/`testValueIdentity()`/`testNullKeyAndViews()`/`testEqualsUsesIdentity()`）。

- 正常：key 引用相等语义。
- 组合：value 引用相等（containsValue/remove(K,V)）、null key、视图联动。
- 边界/异常：内容相等但引用不同视为不同条目；equals 也按引用。
- 健壮性：确定性（固定 new String 对象）；无全局副作用；无文件/网络；跨平台。headless-safe。
