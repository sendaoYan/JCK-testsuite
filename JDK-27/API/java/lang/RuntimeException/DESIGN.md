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

## 契约深化（2026-11，lcO1 批）

双侧 16/16 → 69/69。与 Exception 同款家族矩阵 + 层级特化：RuntimeException instanceof Exception 且 instanceof Throwable、is not Error（Class.isAssignableFrom 负例，避开 javac 不相容 instanceof 编译错）；直接父类精确 == Exception.class；SUID -7034897190745766939L（双侧一致锁定）；null 形态构造、initCause/addSuppressed 精确消息、顶帧精确、printStackTrace 全格式、序列化 round-trip。
