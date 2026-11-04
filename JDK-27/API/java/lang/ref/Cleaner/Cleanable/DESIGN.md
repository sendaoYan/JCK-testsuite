# java.lang.ref.Cleaner.Cleanable — 设计方案

## 1. API 功能
`Cleaner` 的嵌套函数式接口，代表一次已注册的清理动作：
- 唯一抽象方法：`void clean()`（无 checked 异常）——取消注册并执行动作，JDK 提供的实现具有 at-most-once 语义

## 2. Java 用户如何使用
作为 AutoCloseable 风格的资源句柄；也可直接 lambda 实现该接口：
```java
Cleaner.Cleanable c = cleaner.register(obj, () -> release());
c.clean();                 // 显式触发（幂等）
Runnable r = () -> { };    // 也可 Cleaner.Cleanable c = r::run;
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/ref/Cleaner.java`：javadoc 给出 `class Resource implements AutoCloseable` 用 `cleanable.clean()` 实现 close 的范式。
- JDK `src/java.base/share/classes/jdk/internal/ref/PhantomCleanable.java`：`implements Cleaner.Cleanable`，`clean()` final 且带 at-most-once 守卫。
- JDK `test/jdk/java/lang/ref/CleanerTest.java`：大量基于 `Cleanable.clean()` 的显式触发断言。

## 4. 测试用例设计 + 自动化实现
白盒：是接口、嵌套于 Cleaner、唯一抽象方法 `clean()`、无 checked 异常、`isFunctionalInterface()` 为真。
黑盒：
- 方法引用/lambda 直接实现 `Cleanable`，`clean()` 执行动作；
- 匿名类实现可多次执行（接口本身不强制幂等）；
- `Cleaner.register` 返回的真实实现：多次 `clean()` 恰好执行一次；
- 以 `List<Cleanable>` 批量管理并逐一 clean。
对应 `CleanableTest.java` 的 `testBlackBox()`。
