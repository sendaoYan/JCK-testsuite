# java.util.concurrent.atomic.AtomicLongFieldUpdater 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **工厂**：`AtomicLongFieldUpdater.newUpdater(Class, String)`。
- **反射前置校验（JDK-8 实测）**：非 volatile → IAE("Must be volatile type")；int 字段配 long updater → IAE("Must be long type")；static → IAE（消息 null）；private → RuntimeException；不存在/null 参数 → RuntimeException。
- **实例操作**：get/set/lazySet/CAS 两分支/weakCAS/getAndSet + increment/decrement/add/update/accumulate 全族。

## 2. Java 用户如何使用

- 对已有类的 volatile long 字段做原子自增/累加（64 位计数），避免包装 AtomicLong。

## 3. 开源/源码实际用法调研

- OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicLongFieldUpdaterTest.java`。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicLongFieldUpdater`。

## 4. 测试用例设计（映射到 AtomicLongFieldUpdaterTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 正常路径全操作族 | testUpdater |
| P0 | 非 volatile → IAE + "Must be volatile type" | testNewUpdaterValidation |
| P0 | int 字段配 long updater → IAE + "Must be long type" | testNewUpdaterValidation |
| P0 | static / private（嵌套 + java.base 跨类）/ 不存在 / null → 各异常 | testNewUpdaterValidation |
| P0 | 多对象互不干扰 + updater incrementAndGet 64 位环绕（MAX→MIN） | testMultiObjectIndependence |

## 关键规范细节（易错点）

- JDK-8 static 拒绝消息 null（JDK-9+ 有消息）；private 跨类负例两侧稳定。
- long 运算补码环绕无异常。

## 断言规模

PASS (35 assertions)。
