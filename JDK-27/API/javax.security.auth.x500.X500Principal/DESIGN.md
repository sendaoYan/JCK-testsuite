# X500Principal 设计案

## 1. API 功能
`javax.security.auth.x500.X500Principal`（final，实现 `Principal`、`Serializable`）表示 X.500 专有名。
- 常量：`RFC1779`、`RFC2253`、`CANONICAL`。
- 构造器：`X500Principal(String)`、`X500Principal(String, Map<String,String> keywordMap)`、
  `X500Principal(byte[] der)`、`X500Principal(InputStream)`。
- `getName()`（等价 `getName(RFC2253)`）、`getName(String format)`、`getName(String format, Map<String,String> oidMap)`。
- `getEncoded()`（返回克隆）、`toString()`、`equals`/`hashCode`（按 canonical 名）。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/x500/X500Principal.java`。

## 2. Java 用户如何使用
```java
X500Principal p = new X500Principal("CN=Alice, O=Org");
p.getName();                      // "CN=Alice,O=Org"
p.getName(X500Principal.RFC1779); // "CN=Alice, O=Org"
p.getName(X500Principal.CANONICAL); // "cn=alice,o=org"
byte[] der = p.getEncoded();
new X500Principal(der).equals(p); // true
```
典型链路：`new X500Principal(name) → getName/getName(format) → getEncoded → 反序列化/equals/hashCode`。

## 3. 开源代码实际用法调研
- JDK 自带测试（本测试调研过这些文件）：
  - `test/jdk/javax/security/auth/x500/X500Principal/NameFormat.java`：RFC1779/RFC2253/CANONICAL/toString
    的确切输出（如 `cn=duke` → RFC1779 `CN=duke`、CANONICAL `cn=duke`）、非法转义/格式异常。
  - `.../InvalidConstructorInput.java`：非法 DER/流输入抛 `RuntimeException`（实际 `IllegalArgumentException`）。
  - `.../Parse.java`、`.../EscapedChars.java`、`.../KeywordMap.java`、`.../OIDMap.java`：名称解析与自定义关键字/OID。
- JDK 源码：`getName(String)` 对 `null` 或未知格式抛 `IllegalArgumentException`；`getName(String,Map)` 对 null map 抛
  `NullPointerException`；`equals` 比较 canonical 名（大小写不敏感），`getEncoded` 每次克隆。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`X500PrincipalTest.java` 的 `testBlackBox()`）：
- 名称格式：`getName`/RFC2253/RFC1779/CANONICAL/`toString` 的确定输出；格式名大小写不敏感。
- 编码：`getEncoded` 非空且返回克隆（改副本不影响原对象）、`byte[]`/`InputStream` 构造往返相等。
- `(String,Map)` 空 map 等价 String 构造；自定义关键字映射到 OID（RFC1779 输出 `OID.…`）。
- equals/hashCode：输入大小写不同但 canonical 相同则相等且哈希相同；异名/非 Principal 不等。
- 异常：null/未知格式 `IllegalArgumentException`、null oidMap `NullPointerException`、null 名 `NullPointerException`、
  非法名 `IllegalArgumentException`、null keywordMap `NullPointerException`。
白盒补齐：`public final`、实现 `Principal` + `Serializable`。
