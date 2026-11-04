# java.util.concurrent.atomic.AtomicIntegerFieldUpdater 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **工厂**：`AtomicIntegerFieldUpdater.newUpdater(Class, String fieldName)`（protected 构造器，只能工厂创建）。
- **反射前置校验（newUpdater 时，JDK-8 实测）**：
  - 字段非 volatile → `IllegalArgumentException("Must be volatile type")`
  - 字段非 int → `IllegalArgumentException("Must be integer type")`
  - static 字段 → `IllegalArgumentException`（JDK-8 消息 null；JDK-9+ "Must not be a static field"）
  - private 字段不可见 → `RuntimeException`（包装 IllegalAccessException；JDK-8 嵌套类同样拒绝）
  - 字段不存在 → `RuntimeException`（包装 NoSuchFieldException）
  - null 字段名 / null 类 → `RuntimeException`（包装 NPE）
- **实例操作**：get/set/lazySet/compareAndSet/weakCompareAndSet/getAndSet + increment/decrement/add/update/accumulate 全族（作用于传入对象）。

## 2. Java 用户如何使用

- 给已存在的类（不可改源码）加原子字段操作，省一个 Atomic 对象头；热路径计数。

## 3. 开源/源码实际用法调研

- JDK 源码 `java.net.SocketInputStream` 等历史用例；OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicIntegerFieldUpdaterTest.java`（校验矩阵）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicIntegerFieldUpdater`。

## 4. 测试用例设计（映射到 AtomicIntegerFieldUpdaterTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 正常路径全操作族（get/set/CAS 两分支/increment/add/update/accumulate/lazySet） | testUpdater |
| P0 | 非 volatile → IAE + 消息 "Must be volatile type" | testNewUpdaterValidation |
| P0 | long 字段配 int updater → IAE + "Must be integer type" | testNewUpdaterValidation |
| P0 | static 字段 → IAE（JDK-8 消息 null，只断类型） | testNewUpdaterValidation |
| P0 | private 字段（嵌套类与跨类 java.base 两路）→ RuntimeException | testNewUpdaterValidation |
| P0 | 不存在字段 / null 字段名 / null 类 → RuntimeException | testNewUpdaterValidation |
| P0 | 多对象互不干扰（同 updater 两个 POJO） | testMultiObjectIndependence |

## 关键规范细节（易错点）

- protected 字段跨包子类访问场景复杂（本包内即可见），按任务约定跳过。
- 消息断言区分版本：JDK-8 static 拒绝消息为 null，JDK-9+ 起为 "Must not be a static field"。
- JDK-11+ 编译产物有 nestmate 属性，嵌套类 private 可访问；跨类 private 仍是稳定负例（两侧都断言）。

## 断言规模

PASS (34 assertions)。
