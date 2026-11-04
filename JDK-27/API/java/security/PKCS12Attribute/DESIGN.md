# java.security.PKCS12Attribute 黑盒行为测试设计

## 1. API 功能
- 构造：`PKCS12Attribute(String encoded)`、`PKCS12Attribute(String name, String value)`（OID + DER hex）
- 查询：`getName`/`getValue`、`equals/hashCode/toString`

## 2. Java 用户如何使用
KeyStore.Entry.Attribute 实现：PKCS12 密钥库友好名属性 `1.2.840.113549.1.9.20`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PKCS12Attribute.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/PKCS12/MetadataStoreLoadTest.java`（属性随条目存取）

## 4. 测试用例设计 + 自动化实现
- 正常：name/value 构造精确往返；friendlyName OID 常量属性 equals/hashCode
- 组合：setEntry 带 PKCS12Attribute → getEntry 读回同名属性
- 异常：非 OID name → IllegalArgumentException；坏 DER value → IAE

断言入口：`PKCS12AttributeTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化（testContractDeep，本轮新增）
- 结构：final public、实现 KeyStore.Entry.Attribute、恰好 2 构造器（无无参）。
- 构造矩阵：(null,v)/(name,null)/(byte[])null → NPE；非 OID name → IAE "Incorrect format: name"；垃圾/空字节 → IAE "Incorrect format: encoded"。
- DER 精确：("1.2.3.4","value") → 16 字节 30 0E 06 03 2A0304 31 07 0C 05 76616C7565。
- 防御性拷贝三向：getEncoded 每次新数组、改返回数组不影响内部、改构造输入数组不影响内部；unicode 值 UTF-8 DER 往返相等。
- equals/hashCode 值语义矩阵（自反/对称/不同值或名不等/null 与异类型 false/hashCode 一致）；toString 恰为 "1.2.3.4=value"。
