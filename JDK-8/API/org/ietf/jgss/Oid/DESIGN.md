# org.ietf.jgss.Oid 测试设计（JDK-8 侧）

## 1. API 功能
ASN.1 OID 的 GSS-API 封装：标识安全机制与名称类型。
- 构造器：`Oid(String)`（点分十进制）、`Oid(byte[])`（完整 DER，tag 06 + length + 内容）、`Oid(InputStream)`。
- 查询/转换：`toString()`、`getDER()`（返回内部编码的副本）。
- 比较：`equals`/`hashCode`/`containedIn(Oid[])`。
- 非法输入（空串/字母/负分量/多余点）→ `GSSException`，`getMajor()==FAILURE(11)`。

## 2. Java 用户如何使用
```java
Oid krb5 = new Oid("1.2.840.113554.1.2.2");   // Kerberos v5 机制
byte[] der = krb5.getDER();                    // DER-文本对偶
Oid back = new Oid(der);                       // 往返
krb5.equals(back);                             // true
krb5.containedIn(new Oid[]{krb5});             // true
```
核心链路：`new Oid(String) → getDER() → new Oid(byte[]) → equals(原对象)`，DER 与点分文本的规范形式对偶。

## 3. 开源代码实际用法调研
- JDK-8 源码 `jdk/src/share/classes/org/ietf/jgss/*.java`（本仓 `~/git/jdk8u-dev`）：常量定义、构造器校验与 `GSSException` 错误码映射的权威实现。
- JDK 测试 `test/sun/security/krb5/`：以 `Oid`/`GSSManager` 离线构造机制对象与名称，是本包的主要真实用户。
- 本环境离线（无 KDC），黑盒用例只覆盖离线可达路径，在线路径以白盒形态锁为主。
## 4. 测试用例设计 + 自动化实现
- 白盒：public 类、三构造器 + 五方法的精确声明面。
- 黑盒：Kerberos OID 精确字符串；同值 equals/hashCode；异值不等；与 String/null 比较为 false；
  `2.5.13.1` 等含不可规范分量的 OID 的 DER 往返；`"1.2.3"` 与 `"01.02.03"` equals/hashCode/getDER 全等但
  `toString` 保留原始输入（相等性与字面形式分离）；`containedIn` 命中/夹杂/未命中/空数组；
  `getDER` 返回副本（改外层数组不影响后续取值）；非法字符串/非法 DER 字节 → GSSException(FAILURE)。
- 实现见 `OidTest.java`（PASS 计数纳入运行报告）。
