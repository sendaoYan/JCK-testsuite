# java.lang.Exception 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个异常」的冒烟。重点补：白盒结构（父类 Throwable、非接口、
> 四公共构造器存在）、受检层级（非 RuntimeException）、四构造器消息/原因传播、protected 4 参构造器
> （enableSuppression/writableStackTrace 语义）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Exception` 继承 `Throwable`，是受检异常的根（非 RuntimeException）。
- **构造器（4 公共 + 1 protected）**：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`，
  以及 protected `(String,Throwable,boolean,boolean)`。
- **继承的诊断/栈/抑制 API** 来自 `Throwable`。

## 2. Java 用户如何使用

- 自定义受检异常继承 `Exception`，方法签名 `throws`，调用方必须 `catch` 或继续抛出。
- `IOException`、`SQLException`、`ReflectiveOperationException` 均继承 `Exception`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Exception.html`）：四公共构造器。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Exception.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Exception`。

## 4. 测试用例设计（映射到 ExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：父类 Throwable、非接口、四公共构造器 | testWhiteBox |
| P0 | 受检层级（Throwable 且非 RuntimeException） | testHierarchy |
| P0 | 四构造器消息/原因传播（含 (Throwable) 消息 == cause.toString()） | testConstructors |
| P0 | protected 4 参构造器：enableSuppression/writableStackTrace 语义 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **`(Throwable)` 构造器的 `getMessage()` == `cause.toString()`**。
- **protected 4 参构造器**：`enableSuppression=false` 时 `addSuppressed` 静默忽略；
  `writableStackTrace=false` 时 `setStackTrace` 后栈仍为空。

## 注意事项

- headless 安全，不加 `@key`；结果确定。

## 契约深化（2026-11，lcO1 批）

双侧 16/16 → 70/70。四构造矩阵补全（null 形态三件套：(String)null 消息 null 且 toString 无冒号、(Throwable)null 消息与 cause 均 null、(m,null) cause null）；(Throwable) 构造消息 == cause.toString() 逐字；initCause 二次 ISE 前缀 "Can't overwrite cause with" + self IAE "Self-causation not permitted" 逐字；addSuppressed null NPE / self IAE 精确消息；顶帧 className/methodName 精确；printStackTrace 首行/\tat/Suppressed/Caused by 全断言；protected 4 参构造器存在（getDeclaredConstructor）；Exception 非 RuntimeException 非 Error（checked 语义）；SUID -3387516993124229948L（ObjectStreamClass 锁定，双侧一致）；java.lang.Exception 实例本身序列化 round-trip（message/cause/suppressed/toString 保持）。
