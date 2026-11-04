# java.lang.ref.SoftReference 测试设计案（JDK-8）

> 覆盖目标：完整契约。两个构造器、get 保留引用（软引用仅在内存压力下清除，本测试不施压）、clear、null 引用/队列语义。

## 1. API 功能

- `SoftReference(T)`、`SoftReference(T, ReferenceQueue)`；继承 get/clear/enqueue。

## 2. 用户如何使用

- 软引用比弱引用更抗回收，适合做内存敏感缓存。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/SoftReference.html）。
- JCK runtime-8b `tests/api/java_lang/ref/SoftReference/`。

## 4. 测试用例设计（映射到 SoftReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器/get/clear/null 语义 | testSoftReference |

## 易错规范细节（已锁入断言）

- `new SoftReference<>(null)`/`(null,q)`/`(o,null)` 均不抛 NPE；无队列 enqueue 返回 false。
