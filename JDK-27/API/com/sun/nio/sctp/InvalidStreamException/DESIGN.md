# InvalidStreamException 测试设计

## 1. API 功能
`com.sun.nio.sctp.InvalidStreamException` 是非受检异常，继承 `java.lang.IllegalArgumentException`。当尝试向非法流号发送消息时抛出。

构造器：`InvalidStreamException()`、`InvalidStreamException(String msg)`。

**重要发现（JDK-27 javap + 源码核实）**：与 brief 中“断言 streamNumber/association getter”的提示不同，JDK-27 的 `InvalidStreamException` 只是纯异常标记，**没有** `streamNumber()` / `association()` 等 getter，也无额外字段（仅 `serialVersionUID`）。因此在测试中显式断言其不存在这些 getter。

## 2. Java 用户如何使用
```java
try {
    channel.send(buf, messageInfo); // messageInfo 的流号越界
} catch (InvalidStreamException e) {
    System.out.println(e.getMessage());
}
```

作为 `IllegalArgumentException` 子类，可被统一捕获。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/InvalidStreamException.java`：仅两个构造器，`super(msg)`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/MessageInfoImpl.java`：`streamNumber(int)` 对 `streamNumber < 0 || streamNumber > 65536` 抛 `IllegalArgumentException`（实际是流号校验，不直接抛 `InvalidStreamException`）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`InvalidStreamExceptionTest.java` 的 `testBlackBox()`）：

- 消息回传、`instanceof IllegalArgumentException`/`RuntimeException`、`getCause()==null`。
- 无参构造消息为 null、实例独立、可被 `catch (IllegalArgumentException)` 捕获。

白盒（`testWhiteBox()`）：父类为 `IllegalArgumentException`、两个构造器存在、**无** `streamNumber`/`association` getter、`serialVersionUID` 字段存在。
