# java.net.URL 测试设计案（JDK-8）

> 覆盖目标：完整契约。组件访问器、6 个构造器（含 deprecated 与 URLStreamHandler 变体）、
> 默认端口解析、相对解析、equals/hashCode/sameFile 默认端口归一化、toExternalForm/toURI、
> MalformedURLException/NPE/URISyntaxException 精确路径、序列化、file: URL 本地真连接、
> 自定义 URLStreamHandlerFactory 注册、URLConnection 状态机。网络仅限本地 file: URL。

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

## 缺口分析与深化（批次 B，2026-11-05）

原版仅 47/59 断言且全部离线。深化后 345/358 断言，新增维度：

| 深化点 | 方法 | 探明事实 |
|---|---|---|
| 端口边界矩阵 | testPortBoundaries | 65535/65536/99999/0 全部合法且 getPort 原样保留；-1=默认；-2 → MUE "Invalid port number :-2"（spec 与 4 参一致）；spec `:xx` → MUE 且消息含 "xx" |
| 协议校验矩阵 | testProtocolValidationDeep | 4 参 null 协议→NPE；spec null→MUE（非 NPE）；"ht tp"/"1http"/""（4 参）→"unknown protocol: X"；spec 数字开头/空格→"no protocol: X"；"a1+b:c" 把 a1+b 当协议名；"HTTP://".getProtocol()=="http" |
| host 形态 | testHostForms | 4 参 null host→getHost null（spec 三斜杠→""）；IPv6 字面量保留括号、authority "[::1]:80"；"[::1]" equals "[0:0:0:0:0:0:0:1]"（同地址） |
| 上下文解析全矩阵 | testContextResolutionMatrix | RFC 3986 全样例：#f 保留 query、空 spec 复制、../ 与 ../../ 与越顶保留、./、//g、?y（path 变目录加 /）、绝对 spec 覆盖、目录/root/frag 上下文、userinfo+port 继承、null ctx 绝对/相对 |
| 4 参 file 拆分 | testFourArgFileQuerySplit | "/p?q=1#f"：path=/p、query=q=1、ref=f、file="/p?q=1"（不含 ref） |
| 默认端口矩阵 | testDefaultPortsMatrix | http 80/https 443/ftp 21/file -1/jar -1/mailto -1 |
| jar: URL | testJarUrls | file=path="file:/x.jar!/y.txt"、authority null、host ""、ref 提取、query 进 file 且 getQuery 暴露；嵌套 jar → MUE "Nested JAR URLs are not supported" |
| equals 深矩阵 | testEqualsHashCodeDeep | **默认端口归一化**（implicit equals http:80 双向，hashCode 一致，sameFile 同）；query 参与 equals 与 sameFile；**userinfo 不参与**；ref 参与 equals 不参与 sameFile；https:443 归一；file: URL equals/sameFile |
| file: 真连接 | testFileUrlConnections | 临时文件精确字节数 getContentLength/Long、.txt→text/plain、.bin→application/octet-stream、lastModified==File.lastModified、getInputStream 两次同对象、openStream 全文、getContent 为 InputStream、空文件读 EOF、不存在→FileNotFoundException（connect 与 getInputStream 双路径） |
| 自定义 factory | testStreamHandlerFactory | 注册 bproto 协议成功解析；openConnection/openStream/getContent 均委托自定义 handler（UnsupportedOperationException 透传）；factory 返回 null 回退内置 handler；**二次 set → Error "factory already defined"** |
| URLConnection 状态机 | testCustomHandlerURLConnection | 5 参构造保留 handler（openConnection 返回我们的 stub）；timeout 默认 0、set/get 往返、负值 IAE "timeout can not be negative"；setRequestProperty 覆盖/addRequestProperty 累积（多值列表顺序 JDK8 与 JDK27 相反——只断言集合）；connect 后 set/add → IllegalStateException "Already connected"；getOutputStream → UnknownServiceException "protocol doesn't support output"；未连接 stub 默认值全表（-1/null/0） |
| canonical 往返 | testExternalFormRoundTrips | 12 个 spec toExternalForm/toString 不变形、reparse equals；query-only spec 空 path、file="?q" |
| toURI 矩阵 | testToURIMatrix | raw/decoded path、URL→URI→URL 恒等、mailto opaque |

### 版本差异（探针实测，JDK-8 vs JDK-27）

| 行为 | JDK-8 | JDK-27 |
|---|---|---|
| `new URL((String)null)` 消息 | "null" | helpful NPE 文本（"Cannot invoke ... because \"spec\" is null"）包装为 MUE |
| `http://h:xx/p` 消息 | `For input string: "xx"` | `Error at index 0 in: "xx"` |
| getRequestProperties 多值列表顺序 | [b,a]（后加在前） | [a,b]（插入序） |
| 4 参 null 协议 NPE 消息 | 无消息 | 带消息 |
以上差异只断言异常类型/公共前缀，消息差异记录在案。
