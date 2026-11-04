# java.net.URI 测试设计案（JDK-8）

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

## 5. 深化批次 A 缺口分析（2026-11-05，83 → 726 断言）

原 10 个 test 方法只有单一样例的组件分解 + 少量 resolve/equals 正例，缺：
逐字段解析矩阵、异常三元组精确断言、RFC 引用变换表、对象同一性契约、
转义等价规则、compareTo 排序表、IRI/toASCIIString 行为、构造器边界（registry
authority、port 上界、全 null）。本次追加 17 个 test 方法：

| 方法 | 覆盖 |
|---|---|
| testParseMatrixFull | 19 样例 × 19 列（17 组件 + isAbsolute/isOpaque/toString），区分 null（未定义）与 ""（空串，如 `http://h` path=""、`file:///x` authority=null） |
| testExactExceptionMatrix | 12 个非法样例的 reason/index/input/getMessage 精确断言 + registry authority（`http://h:port/` host=null port=-1、`http://u@h:70000/` port=70000 不校验上界） |
| testResolveReferenceTable | base `http://a/b/c/d;p?q` × 34 条引用（RFC 3986 §5.3 全表） |
| testResolveIdentitySemantics | resolve 返回入参对象的四种情形（绝对/opaque child、opaque base）、孤立 fragment 同/异、相对 base 合并 |
| testRelativizeMatrix | 前缀裁剪、非前缀返回原对象（同一性 `==`）、javadoc 恒等式 round-trip |
| testNormalizeMatrix | 19 条边界（越顶 `..` 保留、尾随 `/..`、空串、`.`） |
| testNormalizationIsIdempotentWhenAlreadyNormal | 已 normal 返回 this（对象同一性）、未 normal 必须新对象、二次 normalize 幂等 |
| testEqualsHashCodeEscapes | raw+hex 折叠规则全矩阵（`%41`≠`A`、`%6e`=`%6E`）、IPv6 压缩不等、equals⇒hashCode |
| testCompareToOrdering | 固定排序表（undefined scheme 最前）+ 字段序（scheme→path→query→fragment，未定义 < 已定义）|
| testToASCIIStringIRI | 非 ASCII host/userInfo/path/query/fragment 全部百分号编码（UTF-8 字节）、round-trip 解码等价但 equals=false |
| testToURLEdgeCases | fragment 保留、`file:///x`→`file:/x`、相对 IAE("URI is not absolute")、urn MFE("unknown protocol") |
| testConstructorEdgeMatrix | 5 参 authority `h:80`、全 null→空 URI、4 参 null host、registry authority、7 参 port 65535/65536 不校验上界 |
| testCreateSemantics | create("")、create(非法)→IAE 且 cause 为 USX（精确三元组 + IAE message==cause message）、create(null) NPE |
| testParseServerAuthorityDeep | registry 端口/方括号/IPv6 future 精确异常；成功路径全部返回 this |
| testSerializationMore | opaque/相对/空/非 ASCII URI round-trip |
| （原有 10 个方法全部保留） | |

### 与 RFC 3986 的已验证偏差（Java 实现 RFC 2396 §5.2，双 JDK 一致）

| 场景 | RFC 3986 | Java 实际 |
|---|---|---|
| `base.resolve("")` | `http://a/b/c/d;p?q` | `http://a/b/c/` |
| `base.resolve("?y")` | `http://a/b/c/d;p?y` | `http://a/b/c/?y` |
| `base.resolve("../../../g")` | `http://a/g` | `http://a/../g`（越顶 `..` 保留）|
| `normalize("/a/b/../../../c")` | `/c` | `/../c` |
| 绝对 path 引用 `/./g`、`/../g` | 先 remove_dot_segments | 原样保留 |
| relativize 需要「向上」 | RFC 可产 `../../g` | 返回 target 本身（永不产 `..`）|

### equals 精确规则（javadoc「raw 形式比较，转义十六进制不分大小写」，实测锁定）

- `%41` ≠ `A`、`%6e` = `%6E`、`%6e` ≠ `n`（不是解码等价！）；round-trip `create(toASCIIString())` 解码相同但 equals=false
- scheme/host 大小写不敏感；userInfo/path/query/fragment/SSP 大小写敏感
- IPv6 压缩展开互不相等（`[::1]` ≠ `[0:0:0:0:0:0:0:1]`），hex 大小写折叠相等（`[::FFFF:1]` = `[::ffff:1]`）
- port -1 ≠ 80；`http://h`（path=""）≠ `http://h/`（path="/"）
- toASCIIString 不做 IDNA/punycode：`http://例え.jp/` → `http://%E4%BE%8B%E3%81%88.jp/`（host 也百分号编码；punycode 属 IDN.toASCII）

### JDK-8 vs JDK-27 行为差异（探针实测）

1. authority 内非法字符的 USX **index**：JDK-8 报 authority 起点（`http://exa mple.com/x`→7），JDK-27 报实际非法字符位置（→10）。reason 均为 "Illegal character in authority"。测试只断言 reason/input。
2. `URI.create(null)` NPE message：JDK-8 为 null，JDK-27 为 "Cannot invoke \"String.length()\" because \"this.input\" is null"。只断言类型。
3. `compareTo` 对 `%6e` vs `%6E`：JDK-8 返回非 0（与 equals==true 不一致），JDK-27 返回 0（一致化）。测试不断言该值。
4. `compareTo(null)` NPE message 双侧不同（类型一致）。公开 API javap 双侧完全一致（36 成员，无新增）。
