# org.ietf.jgss.Oid 测试设计

## 1. API 功能
Oid 表示通用对象标识符（ASN.1 OID），在 GSS-API 中标识机制与名称格式。
- 构造器：`Oid(String)`（点分十进制）、`Oid(byte[])`（含 tag/length 的完整 DER）、`Oid(InputStream)`（同上）。
- 查询/转换：`toString()`（点分字符串）、`getDER()`（完整 DER 字节，返回副本）。
- 比较/工具：`equals(Object)`、`hashCode()`、`containedIn(Oid[])`。

注意：JDK-27 实际只有上述三个构造器，**不存在 `new Oid(int[])`**；非法 OID 抛 `GSSException`，错误码为 `FAILURE`(11)（本类没有 `BAD_OID` 常量）。

## 2. Java 用户如何使用
```java
Oid krb5 = new Oid("1.2.840.113554.1.2.2");
String s = krb5.toString();          // "1.2.840.113554.1.2.2"
byte[] der = krb5.getDER();          // 完整 DER，含 tag 06 + length
Oid again = new Oid(der);            // 往返
if (krb5.equals(again)) { ... }
if (krb5.containedIn(mechs)) { ... } // 查询机制是否支持
```
组合链路：`new Oid(String) → getDER() → new Oid(byte[]) → equals(原对象)`，以及 `containedIn(getMechs())`。

## 3. 开源代码实际用法调研
JDK 自身 `test/jdk` 是主要用户（GSS/Kerberos 测试均以 Oid 表示机制）：
- `test/jdk/sun/security/krb5/auto/Context.java`：`me.x = m.createContext(..., mech, cred, GSSContext.DEFAULT_LIFETIME)`，其中 `mech` 即 `new Oid(...)` 得到的机制 Oid。
- `test/jdk/sun/security/krb5/Krb5NameEquals.java`：用 `new Oid("1.2.840.113554.1.2.2.1")` 构造 Kerberos principal 名称类型。
- JDK 源码 `org/ietf/jgss/GSSName.java`：以 `Oid.getInstance("1.2.840.113554.1.2.1.1")` 初始化 `NT_USER_NAME` 等常量。
- JDK 源码 `org/ietf/jgss/Oid.java`：`getDER()` 内部缓存 `derEncoding` 并 `clone()` 返回。
（以上来源均为 `~/git/jdk25u-dev`，本环境未依赖网络搜索。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`OidTest.testBlackBox()`）：
- 构造 + `toString`：Kerberos OID 精确字符串。
- 相等性/哈希：同值相等、异值不等、与 String/null 比较返回 false。
- 规范形式：`"1.2.3"` 与 `"01.02.03"` 的 `equals`/`hashCode`/`getDER` 相等（基于 DER 规范化），但 `toString` 保留原始输入——区分“相等”与“字面形式”。
- `containedIn`：命中/未命中/空数组。
- `getDER` 往返（byte[] 与 InputStream）+ 返回副本语义（改返回数组不影响后续取值）。
- 非法输入：空串/字母/负分量/多余点/前导尾随空格 → `GSSException`，`getMajor()==FAILURE`；非法 DER 字节同样抛 `FAILURE`。
白盒（`testWhiteBox()`）：public 类、非接口、继承 Object、三个构造器 + 五个方法的形态。
