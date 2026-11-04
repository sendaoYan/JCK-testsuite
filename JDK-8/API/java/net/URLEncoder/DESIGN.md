# java.net.URLEncoder 测试设计案（JDK-8）

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
