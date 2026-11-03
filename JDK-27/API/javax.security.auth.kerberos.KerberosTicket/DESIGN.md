# KerberosTicket 测试设计

## 1. API 功能
`javax.security.auth.kerberos.KerberosTicket` 封装客户端视角的 Kerberos ticket（KDC-REP），实现 `Destroyable`、`Refreshable`、`Serializable`。

- 构造器（11 参）：`(asn1Encoding, client, server, sessionKey, keyType, flags, authTime, startTime, endTime, renewTill, clientAddresses)`。
- 查询：`getClient/getServer/getSessionKey/getSessionKeyType/isForwardable/isForwarded/isProxiable/isProxy/isPostdated/isRenewable/isInitial/getFlags/getAuthTime/getStartTime/getEndTime/getRenewTill/getClientAddresses/getEncoded`。
- 生命周期：`isCurrent()`、`refresh()`、`destroy()`、`isDestroyed()`、`toString/equals/hashCode`。

## 2. Java 用户如何使用
JAAS Krb5 登录模块在 commit 阶段从 KDC 应答或 ccache 读取字段，构造 ticket 放入 Subject：

```java
KerberosTicket t = new KerberosTicket(asn1, client, server, sessionKey,
    18, flags, authTime, startTime, endTime, renewTill, addrs);
boolean current = t.isCurrent();
byte[] enc = t.getEncoded();
t.destroy();
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/KerberosTicket.java`）：构造时 flags 长度不足 32 会补 false；renewTill 仅在 renewable 标志为 true 时保存；`getSessionKey()` 返回 `EncryptionKey`。
- JDK 自带测试 `KerberosTixDateTest.java`：`new KerberosTicket("asn1".getBytes(), client, server, "sessionKey".getBytes(), 1, flags, inDate, inDate, inDate, inDate, null)`，验证 Date 字段不可变、销毁后 getter 为 null、非 renewable ticket 的 refresh 抛 `RefreshFailedException`。
- JDK 自带测试 `KerberosHashEqualsTest.java`：验证 equals/hashCode 覆盖 getEncoded/endTime/client/server/sessionKey/flags/日期等字段。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `KerberosTicketTest.java` 黑盒部分：
1. 最小 ticket 的 11 个 getter（含 flags 补齐 32、renewTill=null、clientAddresses=null、getSessionKey 为 EncryptionKey）。
2. getEncoded/getFlags/三个 Date getter 返回副本（不可变）。
3. 全 true flags 的 renewable ticket：7 个布尔 getter + renewTill + sessionKeyType。
4. `isCurrent()` 用 `Long.MAX_VALUE`/epoch 结束时间做确定性断言。
5. 非 renewable ticket `refresh()` 抛 `RefreshFailedException`（离线安全）。
6. 构造器 5 类 null/非法参数抛 `IllegalArgumentException`。
7. destroy 生命周期：所有 getter 变 null/抛 ISE、toString=="Destroyed KerberosTicket"、refresh 抛 RefreshFailedException。
规避：不调用 renewable ticket 的 `refresh()`（会尝试连接 KDC）。
