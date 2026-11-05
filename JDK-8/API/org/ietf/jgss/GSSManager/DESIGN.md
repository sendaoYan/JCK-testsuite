# org.ietf.jgss.GSSManager 测试设计（JDK-8 侧）

## 1. API 功能
工厂入口：`GSSManager.getInstance()` 静态获取默认实现，派生名称/凭据/上下文。
- 名称：`createName(String, Oid)`、`createName(byte[], Oid)`、带 mech 的重载。
- 凭据：`createCredential(int usage)`、`createCredential(GSSName, lifetime, Oid, int)`。
- 上下文：`createContext(GSSName, Oid, GSSCredential, lifetime)` 等。
- 机制查询：`getMechs()`、`getMechsForName(Oid)`、`getNameTypes(Oid)`。

## 2. Java 用户如何使用
```java
GSSManager m = GSSManager.getInstance();
GSSName n = m.createName("user@REALM", GSSName.NT_USER_NAME);
GSSContext ctx = m.createContext(n, new Oid("1.2.840.113554.1.2.2"), null, GSSContext.INDEFINITE_LIFETIME);
```
## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：public 类、protected 构造器（只能走 getInstance）、全部工厂方法的精确签名面。
- 黑盒：`getInstance()` 两次调用为可复用实例（非 null、类型稳定）；`getMechs()` 离线返回含 Kerberos 的
  非空数组；`getMechsForName(NT_USER_NAME)` 非空；`getNameTypes(krb5)` 含 NT_USER_NAME；
  createName 精确字符串/字节数组往返（toString 含原始字符串）；非法名称类型/null mech 的异常通道。
- 实现见 `GSSManagerTest.java`。
