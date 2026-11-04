# IllegalUnbindException 测试设计

## 1. API 功能
`com.sun.nio.sctp.IllegalUnbindException` 是非受检异常，继承 `java.lang.IllegalStateException`。当尝试解绑一个未绑定的本地地址（`unbindAddress`）时抛出。

构造器：`IllegalUnbindException()`、`IllegalUnbindException(String msg)`。JDK-27 javap 确认除 `serialVersionUID` 外无额外成员。

## 2. Java 用户如何使用
```java
try {
    ch.unbindAddress(inetAddress);
} catch (IllegalUnbindException e) {
    // 该地址未绑定
}
```

同样可作为 `IllegalStateException` 统一捕获。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/IllegalUnbindException.java`：两个构造器。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpChannelImpl.java` / `SctpMultiChannelImpl.java` / `SctpServerChannelImpl.java` 的 `unbindAddress` 在地址未绑定时抛出此异常。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`IllegalUnbindExceptionTest.java` 的 `testBlackBox()`）：

- 消息回传、`instanceof IllegalStateException`/`RuntimeException`、`getCause()==null`。
- 无参构造消息为 null、实例独立、可被 `catch (IllegalStateException)` 捕获。

白盒（`testWhiteBox()`）：父类为 `IllegalStateException`、两个构造器存在、`serialVersionUID` 字段存在。
