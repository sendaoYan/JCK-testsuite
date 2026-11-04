# java.util.Base64 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。重点补齐三种编码（basic/URL/MIME）的正则表、
> `withoutPadding`、`encode(src,dst)`/`decode(src,dst)` 的目标数组 IAE、ByteBuffer 往返、
> MIME 自定义行长/分隔符（null 分隔符 NPE、base64 字符分隔符 IAE、行长<=0 不换行）、
> 非法输入 IAE、null NPE 矩阵。

## 1. API 功能

- **工厂**：`getEncoder/getUrlEncoder/getMimeEncoder/getMimeEncoder(int,byte[])`、
  `getDecoder/getUrlDecoder/getMimeDecoder`。
- **Encoder**：`encode(byte[])`、`encode(byte[],byte[])`、`encodeToString(byte[])`、
  `encode(ByteBuffer)`、`wrap(OutputStream)`、`withoutPadding()`。
- **Decoder**：`decode(byte[])`、`decode(String)`、`decode(byte[],byte[])`、
  `decode(ByteBuffer)`、`wrap(InputStream)`。

## 2. 使用方式

`Base64.getEncoder().encodeToString(data)`；`Base64.getUrlEncoder()`；`Base64.getMimeEncoder()`
（76 列 CRLF）；`Base64.getDecoder().decode(s)`；流式 `wrap(...)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Base64`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Base64.java`：
  `getMimeEncoder(int,byte[])` 先 `requireNonNull(lineSeparator)`，含 base64 字符 → IAE，
  `lineLength <= 0` 返回 RFC4648（不换行）；`encode(src,dst)`/`decode(src,dst)` 在 dst 不足时 IAE；
  `wrap` 先 `requireNonNull`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | basic 正则表 + 空输入 + 去填充 + 长度公式 | testBasic |
| P0 | URL 字母表 `-/_` + 跨字母表拒绝 | testUrlSafe |
| P0 | withoutPadding 去除 `=` + 往返 | testWithoutPadding |
| P0 | MIME 76 列 CRLF 换行 + 解码忽略分隔符 | testMime |
| P0 | MIME 自定义行长/分隔符 + null NPE + base64 字符 IAE + 行长<=0 | testMimeCustom |
| P0 | encode/decode 到目标数组（返回写出数 + 目标太小 IAE） | testEncodeIntoArray |
| P1 | ByteBuffer 往返 | testByteBuffer |
| P1 | wrap(OutputStream) 流编码 | testWrapStream |
| P0 | 非法输入 IAE（非法字符、长度 1 mod 4） | testInvalidDecode |
| P0 | null NPE 矩阵（encode/decode/encodeToString/wrap/ByteBuffer） | testNullMatrix |
| P0 | 契约深化：RFC 4648 向量/解码错误消息/非规范输入 | 见 Test 内「契约深化」区块 |

## 关键规范细节

- 基础解码器**容忍**缺失 `=`（`decode("aGVsbG8")` 合法）；长度 1 mod 4 → IAE。
- 基础字母表 `+` `/`；URL 字母表 `-` `_`；基础解码器拒绝 `-`/`_`，URL 解码器接受。
- `getMimeEncoder(int, byte[])`：`lineSeparator` 为 null → NPE；含 base64 字母表字符 → IAE；
  `lineLength <= 0` → 不换行（返回基础编码器）。
- `encode(byte[] src, byte[] dst)` / `decode(byte[] src, byte[] dst)`：dst 空间不足 → IAE；
  返回实际写出的字节数。
- 编码长度公式 `4*ceil(n/3)`；MIME 默认 76 列 CRLF。

## 注意事项

确定性；无墙钟/随机/全局副作用；headless-safe 不加 `@key`；流用 ByteArrayOutputStream 不触文件。
