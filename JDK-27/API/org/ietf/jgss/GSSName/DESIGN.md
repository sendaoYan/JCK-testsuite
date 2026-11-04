# org.ietf.jgss.GSSName 测试设计

## 1. API 功能
GSSName 表示 GSS-API 主体实体（接口）。本用例聚焦常量（标准名称类型 Oid）与通过默认 manager 的名称构造/比较/导出往返，不发起 Kerberos 认证、不联网。
- 常量：`NT_USER_NAME`、`NT_HOSTBASED_SERVICE`、`NT_MACHINE_UID_NAME`、`NT_STRING_UID_NAME`、`NT_ANONYMOUS`、`NT_EXPORT_NAME`（均为 Oid）。
- 方法：`equals(GSSName)`/`equals(Object)`/`hashCode()`、`canonicalize(Oid)`、`export()`、`toString()`、`getStringNameType()`、`isAnonymous()`、`isMN()`。

## 2. Java 用户如何使用
```java
GSSManager m = GSSManager.getInstance();
GSSName n = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
String printable = n.toString();          // "user@EXAMPLE.COM"
Oid type = n.getStringNameType();         // NT_USER_NAME
boolean mn = n.isMN();                    // true
byte[] exp = n.export();
GSSName again = m.createName(exp, GSSName.NT_EXPORT_NAME);
boolean same = n.equals(again);           // true
```
组合链路：`createName(String, NT_USER_NAME) → toString/getStringNameType/isAnonymous/isMN → equals/hashCode → canonicalize → export → createName(byte[], NT_EXPORT_NAME) → equals`。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/krb5/auto/Context.java`：`m.createName(target, GSSName.NT_HOSTBASED_SERVICE)`、`m.createName(someone, GSSName.NT_USER_NAME)`。
- JDK 源码 `org/ietf/jgss/GSSName.java`：用 `Oid.getInstance("1.2.840.113554.1.2.1.1")` 初始化 `NT_USER_NAME`，接口 Javadoc 演示 `export`/`createName(byte[], NT_EXPORT_NAME)` 往返。
- JDK 源码 `sun/security/jgss/GSSNameImpl.java`：默认实现。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`GSSNameTest.testBlackBox()`）：
- 六个常量非 null 且 `toString()` 为精确 OID 值；`NT_USER_NAME.equals(new Oid(...))`；六常量两两互异。
- 用 `m.createName("user@EXAMPLE.COM", NT_USER_NAME)`（显式 realm，无需 krb5.conf）构造确定性名称：`toString`/`getStringNameType`/`isAnonymous==false`/`isMN==true`。
- 两个同名对象 `equals` 为 true 且 `hashCode` 相等。
- `canonicalize(krb5)` 与原对象相等。
- `export()` 非空、确定；`createName(exported, NT_EXPORT_NAME)` 与原对象相等且为 MN。
白盒（`GSSNameTest.testWhiteBox()`）：public interface、无父接口、六个 `public static final Oid` 字段、八个方法。
实测注意：接口**没有 `GSSName.DEFAULT` 常量**；`createName("user", NT_USER_NAME)`（无 realm）在无 Kerberos 配置时抛 `BAD_NAME`，故用显式 realm 形式。
