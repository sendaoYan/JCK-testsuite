# java.nio.charset.CharsetDecoder 测试方案

## 1. API 功能
- 查询: `charset()`, `replacement()`, `averageCharsPerByte()`, `maxCharsPerByte()`, `malformedInputAction()`, `unmappableCharacterAction()`
- 自动检测表面: `isAutoDetecting()`, `isCharsetDetected()`, `detectedCharset()`
- 配置: `replaceWith(String)`, `onMalformedInput(CodingErrorAction)`, `onUnmappableCharacter(CodingErrorAction)`, `reset()`
- 解码: `decode(ByteBuffer,CharBuffer,boolean)`, `flush(CharBuffer)`, `decode(ByteBuffer)` (便捷)

## 2. Java 用户如何使用
- 构造: `StandardCharsets.UTF_8.newDecoder()`.
- 组合链路: `newDecoder -> onMalformedInput 配置 -> reset -> decode(in,out,true) 多步 + flush -> flip 读字符`.
- 便捷: `decode(ByteBuffer)` 一次性解码 (REPORT 下畸形抛 MalformedInputException); decode 尊重 ByteBuffer remaining.

## 3. 开源代码实际用法调研
- `test/jdk/java/io/InputStreamReader/ReadCharBuffer.java`: `UTF_8.newDecoder().onMalformedInput(CodingErrorAction.IGNORE)`.
- `test/jdk/java/io/InputStreamReader/StatefulDecoderNearEOF.java`: `StandardCharsets.UTF_16.newDecoder().onMalformedInput(CodingErrorAction.REPORT)`.
- `test/jdk/java/foreign/TestStringEncoding.java`: `charset.newDecoder()` (解码字符串往返).

## 4. 测试用例设计 + 自动化实现
推导: 默认 metric/replacement/action、自动检测表面 (非自动检测下 isCharsetDetected/detectedCharset
抛 UnsupportedOperationException)、replaceWith、底层 decode+flush、便捷 decode 往返 (CJK/代理对)、
decode 尊重 remaining 区间、畸形字节在 REPORT 下抛异常/REPLACE 下替换为 U+FFFD。
实现于 `CharsetDecoderTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 只测 UTF-8; 畸形字节序列固定; 中文/emoji 固定字面量; 异常用精确类型; 无墙钟/随机/哈希
顺序/全局状态/文件/网络/线程依赖.
