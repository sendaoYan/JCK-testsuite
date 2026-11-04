# KerberosCredMessage 测试设计

## 1. API 功能
`javax.security.auth.kerberos.KerberosCredMessage` 封装 KRB_CRED 消息（RFC 4120 5.8.1），实现 `javax.security.auth.Destroyable`（JDK 9+）。用于把一个主体的 Kerberos 凭证发给另一个主体。

- 构造器：`KerberosCredMessage(KerberosPrincipal sender, KerberosPrincipal recipient, byte[] message)`。
- 查询：`getSender()`、`getRecipient()`、`getEncoded()`（副本）。
- 生命周期：`destroy()`、`isDestroyed()`；`toString`/`equals`/`hashCode`。

注意：JDK-27 实际 API 为上述**构造器**形式，没有 `getKrbCredMessage` 静态工厂、`getMessageType` 或 `KRB_CRED` 常量（以 javap 为准）。

## 2. Java 用户如何使用
GSS-API 扩展接口把凭证交换后交给上层，上层用三个字段重建消息对象：

```java
KerberosCredMessage m = new KerberosCredMessage(sender, recipient, derEncoded);
KerberosPrincipal from = m.getSender();
byte[] der = m.getEncoded();
m.destroy();
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/KerberosCredMessage.java`）：构造器用 `Objects.requireNonNull` + `message.clone()`；`destroy()` 清零内部 message；`hashCode()` 销毁后返回 -1。
- JDK 自带测试 `KerberosNullsAndDestroyTest.java`：`new KerberosCredMessage(c, s, new byte[1])`；null sender/recipient/message 抛 NPE；destroy 后 `getSender/getRecipient/getEncoded` 抛 ISE。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `KerberosCredMessageTest.java` 黑盒部分：
1. 构造 + 三个 getter 往返（sender/recipient/getEncoded 内容）。
2. 构造器复制 message、getEncoded 返回副本。
3. equals/hashCode：sender/recipient/message 字段组合；交换 sender/recipient 不等。
4. toString 含两主体名。
5. null 参数（sender/recipient/message）抛 NPE。
6. destroy 生命周期：isDestroyed、toString=="Destroyed KerberosCredMessage"、hashCode==-1、三个 getter 抛 ISE、销毁对象仅等于自身。
