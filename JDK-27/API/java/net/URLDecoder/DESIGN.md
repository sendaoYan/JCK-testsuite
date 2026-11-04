# java.net.URLDecoder 测试设计案（JDK-27）

> 覆盖目标：完整契约。`+`/`%20`→空格、`%XX` 十六进制解码、UTF-16 往返、代理对往返、
> 非编码字符原样、非法/不完整/负十六进制转义→IllegalArgumentException、null/空编码名 NPE/UEE。

## 1. API 功能

- `decode(String)`（deprecated，UTF-8）、`decode(String, String enc)`。
- 规则：`+`→空格；`%XX`→对应字节；非法转义抛 IllegalArgumentException。

## 2. 用户如何使用

- 与 `URLEncoder.encode` 配对；解码查询串中的 `+` 与 `%XX`。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/URLDecoder.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/URLDecoder/`（URLDecoderArgs、B6463990、EncodeDecode）、
  `~/git/jdk8u-dev/jdk/test/java/net/URLEncoder/DecodeNonEncoded.java`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/URLDecoder/`。

## 4. 测试用例设计（映射到 URLDecoderTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | `+`/`%20`/`%26`/`%2B`/非 ASCII | testBasic |
| P0 | 非编码字符原样（`#`、`X@Y` 等） | testNonEncodedUntouched |
| P0 | UTF-8/UTF-16 往返（历史 `+` bug 4407610） | testRoundTrip |
| P0 | 代理对往返 | testSurrogatePairs |
| P0 | 非法转义矩阵（`%`、`%A`、`%-1` 等）→IAE + null/空编码名 | testValidation |
| P1 | deprecated 单参 == UTF-8 | testDeprecatedDefault |

## 易错规范细节（已锁入断言）

- 编码名 null→NPE；空串→UnsupportedEncodingException；输入 null→NPE。
- 非法十六进制（含负值 `%-1`、不完整 `%`、`%A`）→IllegalArgumentException。
- `+` 只在解码时转空格；`%2B` 解码为字面 `+`。

## JDK-27 补充

- `decode(String, Charset)`（JDK 10+）；`decode(String,(Charset)null)`→NPE。
