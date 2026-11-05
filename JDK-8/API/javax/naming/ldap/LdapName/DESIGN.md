# javax.naming.ldap.LdapName 设计案

## 1. API 功能
`implements javax.naming.Name`，RFC 2253 LDAP 可分辨名。
- 构造器：`LdapName(String)`、`LdapName(List<Rdn>)`。
- 查询：`size()`、`isEmpty()`、`get(int)`、`getRdn(int)`、`getRdns()`、`getAll()`、`getPrefix(int)`、`getSuffix(int)`、`startsWith`/`endsWith`（Name 与 List<Rdn> 重载）。
- 修改：`add(String/Rdn)`、`add(int, String/Rdn)`、`addAll(Name/List)`、`addAll(int, Name/List)`、`remove(int)`。
- 工具：`clone()`、`toString()`、`equals`、`hashCode`、`compareTo(Object)`。
- 内部 RDN 顺序：index 0 为最不具体（最右端）分量；`toString()` 按最具体在前输出。

## 2. Java 用户如何使用
典型：`new LdapName("cn=John,dc=example,dc=com")` → `get(0)`（"dc=com"）/`getRdns()`/`startsWith`/`endsWith` → `add("ou=People")`/`addAll`/`remove(int)` → `clone`/`compareTo`/`equals`。
`startsWith` 判定最不具体（最右）前缀；`endsWith` 判定最具体（最左）后缀。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/test/jdk/javax/naming/ldap/LdapName/` 系列：`NameTests.java`、`LdapNameConstruction.java`（`new LdapName(new ArrayList<>())`、`new LdapName(rdns)`）、`CompareToEqualsTests.java`、`LdapParserTests.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/LdapName.java`。

## 4. 测试用例设计
黑盒断言（LdapNameTest.testBlackBox）：
- 解析与顺序：size/get(0..2)/getRdn/getRdns/getAll/toString。
- getPrefix/getSuffix；startsWith/endsWith（含负例）。
- equals/hashCode/compareTo/clone。
- add/add(int)/addAll/addAll(int)/remove 组合。
- 空名 isEmpty/size/toString。
- 非法名 `new LdapName("=John")` 抛 `InvalidNameException`。
