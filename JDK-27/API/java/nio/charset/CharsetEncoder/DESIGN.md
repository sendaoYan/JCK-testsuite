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

### 第 4 部分增补（A3 批次：契约深化）

1. **不可映射×Action 矩阵**（长度双侧实测一致）: US-ASCII×{"a中b"=1,"é"=1,"\u0080"=1,"😀"=2（代理对
   记一个长度 2 单元）}、ISO-8859-1×{"中"=1,"€"=1,"Ā"=1}——REPORT: 低层 CoderResult.isUnmappable
   +length()==n+互斥断言；便捷 encode 抛 UnmappableCharacterException（getInputLength()==n、
   message="Input length = n"）。ISO-8859-1 对 U+0000..U+00FF 全映射（é→0xE9）。
2. **畸形（孤立代理）**: {\uD800,\uDC00,a\uD800b,\uD83D,a\uDC00}×{UTF-8,US-ASCII} 低层
   MALFORMED len=1；便捷 encode \uD800 抛 MalformedInputException len=1；合法代理对干净编码。
3. **REPLACE/IGNORE 输出**: US-ASCII "a中b"→"a?b"/"ab"；UTF-8 "a\uD800b"→"a?b"/"ab"；
   ISO-8859-1 "中"→"?"/""（REPLACE 走 replacement 字节而非抛）。
4. **字节布局精确表**: UTF-8 类别长度 1/2/3/4，é=C3A9、€=E282AC、中=E4B8AD、😀=F09F9880、混合
   10 字节；UTF-16BE "A"=0041、中=4E2D、😀=D83DDE00（无 BOM）；UTF-16LE 镜像；UTF-16（默认名）
   先 FE FF（A=FEFF0041、中=FEFF4E2D）。
5. **配置**: 三 Action×onMalformedInput/onUnmappableCharacter 返回 this+getter；六常量默认
   replacement 精确字节表（单字节集 3F，UTF-16BE/16=FFFD、UTF-16LE=FDFD——**非单字节 '?'**，
   实测纠正）；replaceWith null/空/超长/非法 UTF-8 字节 {80}→IAE；isLegalReplacement('?')=true、
   ({80})=false；canEncode 边界（\u007F/\u0080、\u00FF/\u0100、空序列 true、null→NPE）；
   avg/maxBytesPerChar 精确表（1/1、1/1、1.1/3、2/2、2/2、2/4）+不变式。
6. **状态机**: flush(fresh)→ISE；encode(true)后 encode(false)→ISE；flush 两次 UNDERFLOW；
   flush 后 encode→ISE；reset 返回 this。
7. **构造+flush 框架**: protected 双构造（3 参与 4 参 replacement 版）反射；子类 super(cs,1,3)
   实例化+度量往返；五组非法参 → IAE。自定义 TailEncoder（implFlush 发 4 字节尾）验证 flush 框架:
   小缓冲 flush→OVERFLOW 且状态仍 END（此后 encode(true) 合法，对照源码）、大缓冲重试→UNDERFLOW+
   尾字节精确、已完成 flush 后 encode→ISE、幂等 flush 不再调 implFlush（1 字节缓冲仍 UNDERFLOW）、
   reset 后全管线 "ab"+E7E8E9EA 精确 6 字节。
8. **手写小缓冲循环**（4 字节原子单元）== 便捷 encode 字节一致（UTF-8/US-ASCII）；ISO-8859-1
   0..255 编码恒等。
