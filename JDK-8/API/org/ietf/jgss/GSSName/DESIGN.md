# org.ietf.jgss.GSSName 测试设计（JDK-8 侧）

## 1. API 功能
接口：带名称类型的主体名。
- 常量：`NT_USER_NAME/NT_HOSTBASED_SERVICE/NT_EXPORT_NAME/NT_ANONYMOUS` 等名称类型 OID 哨兵；
  `GSSName.NT_MACHINE_UID_NAME/NT_STRING_UID_NAME`。
- 方法：`equals(Object)`、`equals(GSSName)`、`canonicalize(Oid)`、`export()`、`toString`、
  `getStringNameType()`、`isAnonymous()`、`duplicate()`、`compare` 族。

## 2. Java 用户如何使用
```java
GSSName a = mgr.createName("host@server.example", GSSName.NT_HOSTBASED_SERVICE);
GSSName b = a.canonicalize(krb5);   // 机制规范形式
byte[] exp = b.export();            // 可存储/传输的导出形式
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：接口面（非类）、全部方法的精确签名、名称类型常量的 OID 精确字符串
  （如 NT_USER_NAME = 1.2.840.113554.1.2.1.1）。
- 黑盒：离线 createName 的 equals/hashCode/toString；NT 化名称的 canonicalize 同一性（单机制下）；
  export 字节非空且可再构造；anonymous 名称 `isAnonymous()`；不同 NT 构造的同文本名不等。
- 实现见 `GSSNameTest.java`。
