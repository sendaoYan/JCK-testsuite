# java.security.KeyStore.Entry.Attribute 黑盒行为测试设计

## 1. API 功能
- 接口：`getName()`、`getValue()`；PKCS12Attribute 是标准实现

## 2. Java 用户如何使用
PKCS12 属性（friendlyName OID 1.2.840.113549.1.9.20、localKeyID 2.16.840.1.113894.746875.1.1）随条目持久化。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PKCS12Attribute.java`、`/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/PKCS12/MetadataStoreLoadTest.java`（属性随条目存取）

## 4. 测试用例设计 + 自动化实现
- 正常：PKCS12Attribute name/value 精确往返、同内容 equals/hashCode、不同 name 不等
- 组合：自定义属性随 TrustedCertificateEntry setEntry/getEntry 往返，PKCS12 还合成 friendlyName=别名 等 builtin 属性
- 边界：所有返回属性 name/value 非 null

断言入口：`AttributeTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
