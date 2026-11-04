# RuntimeException — 设计方案

## 1. API 功能
`java.lang.RuntimeException` 继承 `Exception`，是非受检异常的根。四个构造器：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`。

## 2. Java 用户如何使用
- 编程错误（`NullPointerException`、`IllegalArgumentException`）都继承它，无需 `throws` 声明。
```java
if (arg < 0) throw new IllegalArgumentException("negative");
```

## 3. 开源代码实际用法调研
- JDK 大量异常（`NullPointerException`、`IllegalStateException`、`ArithmeticException`）继承 `RuntimeException`。
- JDK `test/jdk/java/lang/RuntimeException` 验证层级与构造器。

## 4. 测试用例设计 + 自动化实现
白盒：父类 `Exception`、四个构造器存在。
黑盒：是 `Exception`/`Throwable`；四构造器消息/原因传播。
对应 `<Simple>Test.java` 黑盒部分。
