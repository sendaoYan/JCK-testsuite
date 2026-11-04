# java.lang.Error 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个 Error」的冒烟。重点补：白盒结构（父类 Throwable、非接口、
> 四公共构造器存在）、Error 层级（Throwable 且非 Exception）、四构造器消息/原因传播、protected 4 参
> 构造器（enableSuppression/writableStackTrace 语义）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Error` 继承 `Throwable`，是错误层级（非 Exception）的根。
- **构造器（4 公共 + 1 protected）**：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`，
  以及 protected `(String,Throwable,boolean,boolean)`。

## 2. Java 用户如何使用

- 严重错误（`OutOfMemoryError`、`StackOverflowError`、`AssertionError` 等）继承 `Error`；
  通常不被应用捕获。
- `Error` 与 `Exception` 互斥（一个对象不可能同时是两者）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Error.html`）：四公共构造器。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Error.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Error`。

## 4. 测试用例设计（映射到 ErrorTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：父类 Throwable、非接口、四公共构造器 | testWhiteBox |
| P0 | Error 层级（Throwable 且非 Exception） | testHierarchy |
| P0 | 四构造器消息/原因传播 | testConstructors |
| P0 | protected 4 参构造器：enableSuppression/writableStackTrace 语义 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **`(Throwable)` 构造器的 `getMessage()` == `cause.toString()`**。
- **`Error` 不是 `Exception`**（两者互斥）。
- **protected 4 参构造器**：`enableSuppression=false` 时 `addSuppressed` 静默忽略；
  `writableStackTrace=false` 时 `setStackTrace` 后栈仍为空。

## 注意事项

- headless 安全，不加 `@key`；结果确定。
