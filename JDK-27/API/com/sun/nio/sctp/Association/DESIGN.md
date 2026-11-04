# Association 测试设计

## 1. API 功能
`com.sun.nio.sctp.Association` 表示一个 SCTP 关联。它是具体类，唯一构造器为 `protected Association(int associationID, int maxInStreams, int maxOutStreams)`，三个只读 final 访问器：

- `int associationID()`
- `int maxInboundStreams()`
- `int maxOutboundStreams()`

JDK-27 javap 确认：三个方法均 `final` 返回 `int`；私有 final 字段 `associationID`/`maxInStreams`/`maxOutStreams`。

## 2. Java 用户如何使用
用户通常不直接 new（构造器 protected），而是从 `SctpChannel.association()` 或 `SctpMultiChannel.associations()` 获得实例。测试中通过匿名子类调用 protected 构造器：

```java
Association a = new Association(1, 2, 3) {};
int id = a.associationID();
int in = a.maxInboundStreams();
int out = a.maxOutboundStreams();
```

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/Association.java`：文档 + 三个 final 访问器实现（直接返回字段，无校验）。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/AssociationImpl.java`：内部实现继承 `Association`，`super(assocId, maxInStreams, maxOutStreams)`。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/MessageInfoTests.java`：`static class TestAssociation extends Association { TestAssociation(...) { super(...); } }` 用于构造测试关联。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpChannelImpl.java` / `SctpMultiChannelImpl.java`：从 native 关联信息构造 `AssociationImpl` 并作为 `association()` 返回值。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AssociationTest.java` 的 `testBlackBox()`）：

- 三访问器与构造参数精确一致（1/2/3、42/100/200）。
- 边界：`0`、`Integer.MAX_VALUE`、负数——构造器不校验，访问器必须原样回传。

白盒（`testWhiteBox()`）：具体类、继承 `Object`、protected 三参构造器、三方法存在且 `final` 返回 `int`、私有字段存在。
