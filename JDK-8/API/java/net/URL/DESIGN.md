# java.net.URL 测试设计案（JDK-8）

> 覆盖目标：完整契约。组件访问器、6 个构造器（含 deprecated 与 URLStreamHandler 变体）、
> 默认端口解析、相对解析、equals/hashCode/sameFile 大小写规则、toExternalForm/toURI、
> MalformedURLException/NPE/URISyntaxException 精确路径、序列化。绝不 openConnection/openStream/getContent。

## 1. API 功能

- 构造器（6）：`URL(String)`、`URL(String,String,int,String)`、`URL(String,String,String)`（deprecated）、
  `URL(URL,String)`、`URL(URL,String,URLStreamHandler)`、`URL(String,String,int,String,URLStreamHandler)`。
- 组件：`getProtocol/getHost/getPort/getDefaultPort/getPath/getQuery/getRef/getFile/getUserInfo/getAuthority`。
- 语义：`equals/hashCode/sameFile`、`toString/toExternalForm`、`toURI`、Serializable。

## 2. 用户如何使用

- `new URL("http://host:port/path?query#ref")` 解析；用 getFile 拿 path+query；getDefaultPort 拿协议默认端口。
- 相对解析用 `new URL(context, spec)`；自定义协议用带 URLStreamHandler 的构造器（离线可测，不 openConnection）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/URL.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URL/`（Constructor、ParseURL、GetDefaultPort、Equals、
  HashCode、SpecifyHandler、RelativeURL、TestPort）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/URL/`。

## 4. 测试用例设计（映射到 URLTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 组件访问器 + file/authority/userInfo | testComponents |
| P0 | 缺省组件（空 path/file、null query/ref/userInfo） | testEmptyComponents |
| P0 | 默认端口（http=80、file=-1） | testDefaultPort |
| P0 | 4/3 参构造器 + 相对解析（RFC 示例）+ URLStreamHandler 变体 | testConstructors |
| P0 | 异常矩阵：无协议/未知协议→MFE；端口<-1→MFE；null 协议→NPE | testValidation |
| P0 | equals/hashCode host 大小写不敏感 + sameFile 忽略 ref | testEqualsHashCodeSameFile |
| P1 | toURI + 未编码空格→URISyntaxException | testToURI |
| P1 | 序列化 round-trip | testSerialization |

## 易错规范细节（已锁入断言）

- `equals/hashCode/sameFile` **会解析 host（hostname 触发 DNS）**，故这些断言一律用 IP 字面量（127.0.0.1）保持离线确定。
- `sameFile` 忽略 ref（fragment）；`getFile == path + "?" + query`。
- `URL(String,int…)` 4 参端口 `<-1` 抛 MalformedURLException；端口上界（65536）**不校验**。
- `new URL("http://example.com/a b").toURI()` 抛 URISyntaxException（URL 不自动编码空格）。
