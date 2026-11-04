# java.nio.charset.CharsetEncoder 测试方案

## 1. API 功能
- 查询: `charset()`, `replacement()`, `averageBytesPerChar()`, `maxBytesPerChar()`, `malformedInputAction()`, `unmappableCharacterAction()`
- 配置: `replaceWith(byte[])`, `isLegalReplacement(byte[])`, `onMalformedInput(CodingErrorAction)`, `onUnmappableCharacter(CodingErrorAction)`, `reset()`
- 编码: `encode(CharBuffer,ByteBuffer,boolean)`, `flush(ByteBuffer)`, `encode(CharBuffer)` (便捷)
- 判定: `canEncode(char)`, `canEncode(CharSequence)`

## 2. Java 用户如何使用
- 构造: `StandardCharsets.UTF_8.newEncoder()`.
- 组合链路: `newEncoder -> onMalformedInput/onUnmappableCharacter 配置 -> reset -> encode(in,out,true) 多步 + flush -> flip 读字节`.
- 便捷: `encode(CharBuffer)` 一次性编码 (REPORT 下遇畸形/不可映射抛 CharacterCodingException).

## 3. 开源代码实际用法调研
- `test/jdk/tools/launcher/DisableBestFitMappingTest.java`: `Charset.forName("native.encoding").newEncoder()`.
- `test/jdk/java/foreign/TestStringEncoding.java`: `charset.newEncoder().canEncode(testString)` — canEncode 预检.
- `test/jdk/java/io/InputStreamReader/StatefulDecoderNearEOF.java`: 同类 `newDecoder().onMalformedInput(CodingErrorAction.REPORT)`.

## 4. 测试用例设计 + 自动化实现
推导: 默认 metric/replacement/action、replaceWith 与 isLegalReplacement (过长抛 IAE)、底层
encode+flush、便捷 encode 往返、canEncode、malformed (孤代理项) / unmappable (US-ASCII 编码 CJK)
在 REPORT 下返回 CoderResult 或抛 CharacterCodingException, REPLACE 下替换为 '?'。
实现于 `CharsetEncoderTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 只测 UTF-8/US-ASCII; 中文/孤代理项固定字面量; 异常用精确类型; 无墙钟/随机/哈希顺序/
全局状态/文件/网络/线程依赖.
