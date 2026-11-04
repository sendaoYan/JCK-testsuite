# java.security.PKCS12Attribute 测试设计（JDK-8）

## 1. API 功能
- final public 类，实现 `KeyStore.Entry.Attribute`；恰好 2 个构造器 `(String name,String value)`、`(byte[] encoded)`（无无参构造）。
- 不可变值对象：`getName`/`getValue`/`getEncoded`（DER 编码，防御性拷贝）、值语义 `equals`/`hashCode`、`toString` 为 "name=value"。

## 2. Java 用户如何使用
PKCS12 密钥库属性（friendlyName 1.2.840.113549.1.9.20、localKeyId 1.2.840.113549.1.9.21）以 (OID,字符串) 或 DER 字节构造，经 `KeyStore.Entry.getAttributes()` 读出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/security/PKCS12Attribute.java`
- JDK 测试 `/data/yansendao/git/jdk8u-dev/jdk/test/java/security/KeyStore/PKCS12/`
- 内部使用 `sun.security.pkcs12`（往返编码参照）

## 4. 测试用例设计 + 自动化实现
- 结构：final、实现 Attribute、恰好 2 构造器。
- 构造矩阵：`(null,v)`/`(name,null)`/`(byte[])null` → NPE；`("not-an-oid",v)` → **IAE "Incorrect format: name"**；`("1.2.x",v)` → IAE；`{1}` 垃圾字节与空数组 → **IAE "Incorrect format: encoded"**。
- DER 精确字节：`("1.2.3.4","value")` → `30 0E 06 03 2A0304 31 07 0C 05 76616C7565`（SEQUENCE{OID, SET{UTF8String}}，16 字节）。
- 防御性拷贝：`getEncoded()` 每次新数组、改返回数组不影响内部、改构造输入数组不影响内部。
- 往返：byte[] 构造器 round-trip name/value/equals（对称）；unicode 值（"välue-π"）经 UTF-8 DER 往返相等。
- equals/hashCode：自反/对称、不同值或不同名不等、`equals(null)`/非属性类型 false、相等实例 hashCode 一致。
- `toString()` 恰为 "1.2.3.4=value"。

断言入口：`PKCS12AttributeTest.testContractDeep()`（5→28 断言）。
