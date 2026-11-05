# Exception — 设计方案

## 1. API 功能
`java.lang.Exception` 继承 `Throwable`，是受检异常的根。四个构造器：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`。

## 2. Java 用户如何使用
- 自定义受检异常继承 `Exception`，方法签名 `throws`，调用方必须 `catch` 或继续抛出。
```java
class BizException extends Exception { BizException(String m) { super(m); } }
void run() throws BizException { throw new BizException("fail"); }
```

## 3. 开源代码实际用法调研
- `IOException`、`SQLException`、`ReflectiveOperationException` 均继承 `Exception`。
- JDK `test/jdk/java/lang/Exception` 验证受检层级与构造器。

## 4. 测试用例设计 + 自动化实现
白盒：父类 `Throwable`、四个构造器存在。
黑盒：非 `RuntimeException`（受检）；四构造器消息/原因传播。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11，lcO1 批）

双侧 16/16 → 70/70。四构造矩阵补全（null 形态三件套：(String)null 消息 null 且 toString 无冒号、(Throwable)null 消息与 cause 均 null、(m,null) cause null）；(Throwable) 构造消息 == cause.toString() 逐字；initCause 二次 ISE 前缀 "Can't overwrite cause with" + self IAE "Self-causation not permitted" 逐字；addSuppressed null NPE / self IAE 精确消息；顶帧 className/methodName 精确；printStackTrace 首行/\tat/Suppressed/Caused by 全断言；protected 4 参构造器存在（getDeclaredConstructor）；Exception 非 RuntimeException 非 Error（checked 语义）；SUID -3387516993124229948L（ObjectStreamClass 锁定，双侧一致）；java.lang.Exception 实例本身序列化 round-trip（message/cause/suppressed/toString 保持）。
