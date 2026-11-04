# org.ietf.jgss.GSSManager 测试设计

## 1. API 功能
GSSManager 是 GSS-API 对象工厂（抽象类）：查询可用机制与名称类型，并创建 GSSName/GSSCredential/GSSContext。
- 工厂入口：`static GSSManager getInstance()`（默认 `sun.security.jgss.GSSManagerImpl`）。
- 查询：`getMechs()`、`getNamesForMech(Oid)`、`getMechsForName(Oid)`。
- 创建：`createName(String/byte[], Oid[, Oid])`、`createCredential(...)`、`createContext(...)`。
- provider 偏好：`addProviderAtFront/AtEnd(Provider, Oid)`。

## 2. Java 用户如何使用
```java
GSSManager m = GSSManager.getInstance();
Oid[] mechs = m.getMechs();                    // 至少含 Kerberos
Oid[] types = m.getNamesForMech(krb5);         // krb5 支持的名称类型
GSSName n = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
GSSName r = m.createName(n.export(), GSSName.NT_EXPORT_NAME);
```
组合链路：`getInstance → getMechs → getNamesForMech → getMechsForName → createName → export → createName(NT_EXPORT_NAME) → equals`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/krb5/auto/Context.java`：`GSSManager m = GSSManager.getInstance(); me.x = m.createContext(m.createName(target, ...), mech, cred, GSSContext.DEFAULT_LIFETIME);`。
- JDK 源码 `org/ietf/jgss/GSSManager.java`：Javadoc 演示 `createName("duke", GSSName.NT_USER_NAME)` 等完整链路。
- JDK 源码 `sun/security/jgss/GSSManagerImpl.java`：`getInstance()` 返回默认实现。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`GSSManagerTest.testBlackBox()`）：
- `getInstance()` 非 null 且为 GSSManager 实例。
- `getMechs()` 非空且含 Kerberos OID。
- `getNamesForMech(krb5)` 含 `NT_USER_NAME`/`NT_HOSTBASED_SERVICE`/`NT_EXPORT_NAME`。
- `getMechsForName(NT_USER_NAME)` 含 krb5。
- `createName("user@EXAMPLE.COM", NT_USER_NAME)` 的 toString 与 export/re-import 往返。
- `createContext(new byte[0])` 抛 `GSSException` 且 major==`NO_CONTEXT`；`createContext({1,2,3})` 抛 `GSSException`（major 实现相关，仅断类型）。
白盒（`GSSManagerTest.testWhiteBox()`）：public abstract 类、继承 Object、public 无参构造器、static `getInstance`、八个抽象方法名。
