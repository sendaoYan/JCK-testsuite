# java.net.URI 测试设计案（JDK-27）

> 覆盖目标：完整契约，不是「方法被调用一次」的冒烟。补齐构造器矩阵、raw/decoded 组件分解、
> null 语义、异常矩阵、层级 vs 不透明 URI、resolve/relativize/normalize、equals/compareTo 大小写规则、
> 序列化 round-trip。全部离线，无网络/文件/墙钟依赖。

## 1. API 功能

- 构造器（6）：`URI(String)`、`URI(String,int…)` 7 参、`URI(String,String,String,int,String,String,String)`、
  5 参 `(scheme,authority,path,query,fragment)`、4 参 `(scheme,host,path,fragment)`、3 参 `(scheme,ssp,fragment)`。
- 工厂：`create(String)`（URISyntaxException → IAE）。
- 组件访问：`getScheme/getUserInfo/getHost/getPort/getPath/getQuery/getFragment/getAuthority/getSchemeSpecificPart`，
  以及 raw 版本 `getRawUserInfo/getRawPath/...`。
- 语义：`isAbsolute/isOpaque`、`resolve(URI/String)`、`relativize`、`normalize`、`parseServerAuthority`、
  `toURL`、`toASCIIString`、`equals/hashCode/compareTo/toString`、Serializable。

## 2. 用户如何使用

- `new URI(str)` 解析；`URI.create(str)` 在参数非法时抛 IAE 而非受检异常。
- 用 getRaw* 拿原始百分号编码，用 get* 拿解码结果；非 ASCII 字符在 toString 保留原样、toASCIIString 百分号编码。
- `resolve/relativize/normalize` 处理相对路径；`parseServerAuthority` 校验基于 server 的 authority。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/URI.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URI/`（Test.java、RelativeEncoding.java、URItoURLTest.java）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/URI/`（CtorTests、componentsTests、
  rawTests、operationsTests、compareTests、equalsTests、createTests、serial）。

## 4. 测试用例设计（映射到 URITest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 单串构造器组件分解 + isAbsolute/isOpaque/toString | testSingleStringComponents |
| P0 | raw vs decoded（userInfo/authority/path/query/fragment/ssp） | testRawVsDecoded |
| P0 | 非 ASCII 自动处理 + toASCIIString | testNonAsciiAndAscii |
| P0 | 构造器异常矩阵：数字开头 scheme/空格/端口非法→URISyntaxException；create 包 IAE；null→NPE | testConstructorValidation |
| P0 | 3/4/5/7 参构造器 + 端口 -1/0/正/<-1 | testMultiArgConstructors |
| P0 | null 语义：resolve/relativize null→NPE | testNullSemantics |
| P0 | resolve/relativize/normalize RFC 示例 | testResolveRelativizeNormalize |
| P0 | equals/hashCode/compareTo（scheme/host 大小写不敏感、fragment 参与） | testEqualsHashCodeCompareTo |
| P1 | toURL + 相对 URI→IAE + parseServerAuthority | testToURLAndParseServerAuthority |
| P1 | 序列化 round-trip | testSerialization |

## 易错规范细节（已锁入断言）

- 单串构造器里**空格**（authority/path 中）抛 URISyntaxException，但**非 ASCII 字符**允许（raw 保留原样，toString 保留，toASCIIString 编码）。
- 单串构造器**不校验端口上界**（`http://h:99999/` 合法，getPort=99999）；7 参构造器端口仅 `<-1` 抛 URISyntaxException。
- `toURL()` 对相对 URI 抛 IllegalArgumentException（不是 MalformedURLException）。
- getRawUserInfo 保留 `%40`，getUserInfo 解码为 `@`，authority 解码后可能含 `@`。
- 网络边界：`resolve/relativize` 传 null 抛 NPE。

## 5. 深化批次 A 缺口分析（2026-11-05，95 → 738 断言，含保留的 13 项白盒）

与 JDK-8 侧同构，追加 17 个 test 方法（解析矩阵 19×19、异常三元组、RFC 引用
变换表 34 条、resolve/relativize/normalize 同一性、equals 转义规则、compareTo
排序表、IRI、构造器边界、create 语义、parseServerAuthority、序列化）。
关键实测结论与 JDK-8 侧一致，另见 JDK-8 侧 DESIGN.md 第 5 章的偏差表；本侧
差异记录：

- `URI("http://exa mple.com/x")` USX index=10（实际非法字符位置；JDK-8=7）
- `URI.create(null)` NPE 带 message（JDK-8 无）
- `compareTo` 已与 equals 一致（`%6e` vs `%6E` 返回 0）
- 公开 API 与 JDK-8 完全一致（javap 36 成员对照，无新增公开方法）
