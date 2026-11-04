# java.nio.charset.StandardCharsets 测试方案

## 1. API 功能
- 常量: `US_ASCII`, `ISO_8859_1`, `UTF_8`, `UTF_16BE`, `UTF_16LE`, `UTF_16`, `UTF_32BE`, `UTF_32LE`, `UTF_32`
  (9 个 `public static final Charset`)

## 2. Java 用户如何使用
- 直接取常量编解码, 无需 forName: `StandardCharsets.UTF_8.encode(s)` / `.decode(bb)`.
- 组合链路: `UTF_8.encode(String) -> UTF_8.decode(ByteBuffer)` 往返; `UTF_16BE/LE/32BE.encode("A")` 核对字节布局.

## 3. 开源代码实际用法调研
- `test/jdk/tools/jlink/runtimeImage/GenerateJLIClassesTest.java`: `fileString.getBytes(StandardCharsets.UTF_8)`.
- `test/jdk/tools/jmod/JmodTest.java`: `import static java.nio.charset.StandardCharsets.UTF_8;`.
- `test/jdk/java/io/InputStreamReader/StatefulDecoderNearEOF.java`: `StandardCharsets.UTF_16.newDecoder()`.

## 4. 测试用例设计 + 自动化实现
推导: 9 常量名字与单例 (== forName)、UTF-16BE/LE/32BE/32LE 字节布局、UTF_16 BOM、UTF-8 往返
(CJK/代理对)、ISO-8859-1 vs US-ASCII 可编码性、US-ASCII 编码 CJK 抛 CharacterCodingException。
实现于 `StandardCharsetsTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 只测标准字符集; BOM 断言接受两种字节序 (不绑平台); 中文/emoji 固定字面量; 无墙钟/随机/
哈希顺序/全局状态/文件/网络/线程依赖.
