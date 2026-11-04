# IllegalReceiveException 测试设计

## 1. API 功能
`com.sun.nio.sctp.IllegalReceiveException` 是非受检异常，继承 `java.lang.IllegalStateException`（进而 `RuntimeException`）。当 SCTP 通道在非法状态下尝试 `receive` 时抛出。

构造器：`IllegalReceiveException()`、`IllegalReceiveException(String msg)`。JDK-27 javap 确认除 `serialVersionUID` 外无额外成员。

## 2. Java 用户如何使用
```java
try {
    ch.receive(buf, attachment, handler);
} catch (IllegalReceiveException e) {
    // 通道未绑定/未连接等非法接收状态
    System.out.println(e.getMessage());
}
```

作为 `IllegalStateException` 子类，可被统一 `catch (IllegalStateException e)` 捕获。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/IllegalReceiveException.java`：仅两个构造器，`super(msg)` 透传消息。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpChannelImpl.java` / `SctpMultiChannelImpl.java`：在通道未绑定/已关闭时 `throw new IllegalReceiveException("channel not bound");`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`IllegalReceiveExceptionTest.java` 的 `testBlackBox()`）：

- `new IllegalReceiveException("msg")` 的 `getMessage()` 精确回传。
- `instanceof IllegalStateException` 与 `instanceof RuntimeException`。
- `getCause() == null`。
- 无参构造 `getMessage() == null`。
- 两个实例互相独立、可被 `catch (IllegalStateException)` 捕获。

白盒（`testWhiteBox()`）：父类为 `IllegalStateException`、两个构造器存在、`serialVersionUID` 字段存在。
