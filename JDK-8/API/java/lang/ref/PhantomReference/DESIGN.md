# java.lang.ref.PhantomReference 测试设计案（JDK-8）

> 覆盖目标：完整契约。构造器要求队列、get() 恒 null（即使引用存活）、null 引用/队列语义、enqueue/isEnqueued、队列 poll。

## 1. API 功能

- `PhantomReference(T, ReferenceQueue)`（必须带队列）；继承 clear/enqueue/isEnqueued。

## 2. 用户如何使用

- 幽灵引用不保留引用（get 恒 null），仅在对象回收后入队，用于回收后清理资源。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/PhantomReference.html）。
- JCK runtime-8b `tests/api/java_lang/ref/PhantomReference/`。

## 4. 测试用例设计（映射到 PhantomReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | get 恒 null + enqueue/poll | testPhantomReference |

## 易错规范细节（已锁入断言）

- `get()` 恒 null（区别于 Weak/Soft）；`new PhantomReference<>(null,q)`/`(o,null)` 不抛 NPE。
