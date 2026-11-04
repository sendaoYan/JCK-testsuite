# java.net.URLEncoder 测试设计案（JDK-27）

> 覆盖目标：完整契约。空格→`+`、非保留字符透传、其余 `%XX` 大写十六进制 UTF-8、
> UTF-16 往返、代理对 4 字节 UTF-8 展开、deprecated 单参重载、null/空编码名 NPE/UnsupportedEncodingException。

## 1. API 功能

- `encode(String)`（deprecated，UTF-8）、`encode(String, String enc)`。
- 规则：字母数字与 `- _ . *` 透传；空格→`+`；其余字符 UTF-8 字节 → `%XX`（大写十六进制）。

## 2. 用户如何使用

- 表单编码 `application/x-www-form-urlencoded`：`URLEncoder.encode(query, "UTF-8")`。
- 与 `URLDecoder.decode` 配对往返。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/URLEncoder.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URLEncoder/`（URLEncoderEncodeArgs、SurrogatePairs、
  Decoder、URLEncodeDecode）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/URLEncoder/`。

## 4. 测试用例设计（映射到 URLEncoderTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 空格/`&`/非 ASCII + UTF-16（含 BOM） | testBasic |
| P0 | 全字符类编码（非保留透传 vs `%XX`） | testCharacterClass |
| P0 | 代理对 → 4 字节 UTF-8 `%XX` 序列 | testSurrogatePairs |
| P1 | deprecated 单参 == UTF-8 | testDefaultOverload |
| P0 | null/空编码名→NPE/UEE；null 字符串→NPE | testValidation |

## 易错规范细节（已锁入断言）

- 编码名 null→NPE（**不是** UnsupportedEncodingException）；空串 `""`→UnsupportedEncodingException。
- 输入字符串 null→NPE（encode(null)/encode(null,enc)）。
- 代理对按 UTF-8 编码为 4 字节，即 4 个 `%XX`；UTF-16 输出带 BOM（`%FE%FF`）。

## JDK-27 补充

- `encode(String, Charset)`（JDK 10+）；`encode(String,(Charset)null)`→NPE。

## 缺口分析与深化（批次 B，2026-11-05）

原版 25 断言。深化后 192 断言 断言，新增（JDK-27 侧另含 Charset 重载与 ISO-8859-1/US-ASCII Charset 对象一致性断言）：

| 深化点 | 方法 | 探明事实 |
|---|---|---|
| ASCII 33..126 全矩阵 | testAsciiMatrix | 字母数字逐字符透传（62×）；`.` `-` `*` `_` 透传；空格→`+`；其余每个可打印 ASCII 精确 `%XX`（大写）；控制字符 %00/%01/%0D%0A/%7F |
| ISO-8859-1 单字节 | testIso8859AndAliases | ü→%FC、é→%E9、ÿ→%FF；0..255 全字符编码后 %00/%80/%FC/%FF 全部出现；别名 UTF8/utf-8/Iso-8859-1/ISO8859_1 等价；US-ASCII 对非 ASCII 用替换字节 → %3F（不抛异常） |
| UTF-8 边界码点 | testUtf8Boundaries | U+007F 单字节 / U+0080 双字节边界、U+07FF/U+0800、U+FFFF 三字节最大、U+10000 代理对四字节、CJK 样例；编码确定性 |
| 空串与标点表 | testEmptyBoundaries | 空串→空串（含 deprecated 重载）；27 个标点字符精确输出表 |
