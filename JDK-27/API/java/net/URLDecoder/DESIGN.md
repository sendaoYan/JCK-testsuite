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

## 缺口分析与深化（批次 B，2026-11-05）

原版 28 断言。深化后 374 断言 断言，新增（JDK-27 侧另含 Charset 对象重载一致性断言）：

| 深化点 | 方法 | 探明事实 |
|---|---|---|
| %XX 全 256 矩阵 | testHexMatrix256 | ISO-8859-1 下 %00..%FF 每个字节精确解码为对应 char（256 断言）；大小写/混合 hex 均接受；UTF-8 2/3/4 字节序列；UTF-16 BOM 形式 |
| IAE 消息分类 | testIAEMessages | 两类精确消息前缀（两侧一致）："URLDecoder: Incomplete trailing escape (%) pattern"（尾随孤立/不完整 %：`%` `%A` `%4` `abc%` `a%` `a%2` `% `）；"URLDecoder: Illegal hex characters in escape (%) pattern"（非 hex 数字：`%zz` `%1z` `%z1` `%G1` `%%%`）；异常精确类为 IllegalArgumentException |
| + 与混合 | testPlusMixed | 孤立 + →空格、`%2B`→字面 +、+%20 混合、表单风格、原始特殊字符不动、%00→NUL |
| charset 处理 | testCharsetHandling | 畸形 UTF-8（%FF/%C3）解码为 U+FFFD 替换字符不抛；坏 charset：有转义序列时 UEE（消息=charset 名）；空 charset 恒 UEE "URLDecoder: empty string enc parameter"（两侧一致）；ISO-8859-1 0..255 round-trip；UTF-8 样本 round-trip |
| deprecated 双行为 | testDeprecatedDefault | decode(String) 等价 UTF-8 重载（+ 与 %20 双形式） |

### 版本差异（探针实测）

| 行为 | JDK-8 | JDK-27 |
|---|---|---|
| `decode("+", "no-such-charset")` | 不抛（charset 惰性检查：无 % 序列时 charset 从不查找，返回 " "） | UEE（JDK 10+ 构造时 eager 检查） |
| `decode("plain", "no-such")` | 不抛 | UEE |
| Illegal hex 消息细节 | `For input string: "zz"` | `not a hexadecimal digit: "z" = 122` |
JDK-8 侧将惰性行为断言为契约（反映真实实现），JDK-27 侧断言 eager UEE。
