# IllegalClassFormatException — 设计方案

## 1. API 功能
`java.lang.instrument.IllegalClassFormatException`（继承 `Exception`）表示 `ClassFileTransformer.transform` 收到格式非法的 class 文件。构造器 `()` 与 `(String message)`。

## 2. Java 用户如何使用
```java
public byte[] transform(...) throws IllegalClassFormatException {
    if (buffer.length < 4) throw new IllegalClassFormatException("truncated class file");
    return null;
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument` agent 代码在校验字节码失败时抛出该异常。
- ASM/ByteBuddy 的 agent 桥接层按规范抛出 `IllegalClassFormatException`。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Exception + 两个构造器。
黑盒：no-arg message null；String 构造 message/cause；instanceof Exception。
对应 `IllegalClassFormatExceptionTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 构造器面锁定（javap + 反射实证，任务书"有 cause 构造器"猜测被推翻）：仅 () 与 (String) 两个构造器，无 (Throwable)/(String,Throwable) 形态——与 UnmodifiableClassException 的差异点。
- getMessage() 往返：null → null，"x" → "x"。
- 继承链：extends Exception；getCause() 恒 null（无 cause 通道）。
