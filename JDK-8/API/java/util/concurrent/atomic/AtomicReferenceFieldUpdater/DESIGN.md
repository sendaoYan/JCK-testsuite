# java.util.concurrent.atomic.AtomicReferenceFieldUpdater 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **工厂**：`AtomicReferenceFieldUpdater.newUpdater(Class tclass, Class vclass, String fieldName)`（弱类型：vclass 是运行时校验用 Class 参数）。
- **反射前置校验（JDK-8 实测）**：
  - 非 volatile → `IllegalArgumentException("Must be volatile type")`
  - 原始类型字段 → IAE
  - **字段声明类型不是 vclass 的实例 → `ClassCastException`（newUpdater 时抛，消息 null）**
  - static / private / 不存在 / null → 同 int 版（IAE / RuntimeException / RuntimeException / RuntimeException）
- **实例操作**：get/set/lazySet/CAS（identity 语义）/weakCAS/getAndSet + update/accumulate 函数族。

## 2. Java 用户怎么用

- 对已有类的 volatile 引用字段做原子替换（链表头、懒加载指针）。

## 3. 开源/源码实际用法调研

- JDK 源码历史用例；OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicReferenceFieldUpdaterTest.java`（含 CCE 弱类型契约）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicReferenceFieldUpdater`。

## 4. 测试用例设计（映射到 AtomicReferenceFieldUpdaterTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 正常路径 CAS 两分支 | testAtomicReferenceFieldUpdater |
| P0 | 全操作族（getAndSet/weakCAS 两分支/update/accumulate/lazySet） | testOperationFamily |
| P0 | 非 volatile → IAE + "Must be volatile type"；原始类型 → IAE | testNewUpdaterValidation |
| P0 | **字段类型不是 R（弱类型不匹配）→ ClassCastException** | testNewUpdaterValidation |
| P0 | static / private（嵌套 + Thread.name 跨类）/ 不存在 / null → 各异常 | testNewUpdaterValidation |
| P0 | **identity 语义（equals 相等不同对象 CAS 失败）+ null 期望分支** | testIdentitySemantics |
| P0 | 多对象互不干扰 | testMultiObjectIndependence |

## 关键规范细节（易错点）

- 弱类型契约：vclass 与字段声明类型不符在 **newUpdater 时**即抛 CCE（不是首次访问时）。
- CAS 按 == 匹配；null 期望合法。

## 断言规模

PASS (32 assertions)。
