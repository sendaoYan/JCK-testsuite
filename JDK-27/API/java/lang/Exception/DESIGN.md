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
