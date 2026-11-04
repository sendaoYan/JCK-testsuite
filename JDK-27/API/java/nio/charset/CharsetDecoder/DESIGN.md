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

### 第 4 部分增补（A3 批次：契约深化——畸形输入×三 Action 矩阵为最大权重）

1. **UTF-8 畸形单元×Action 矩阵**（表驱动，长度双侧实测一致）: {41,C3,28}(截断 2 字节头,len=1)、
   {E0,80}(len=1)、{ED,A0,80}(CESU-8 代理区,len=3)、{C0,80}(overlong,len=1)、{F5}(>U+10FFFF 起始,
   len=1)、{41,F5,42,C0,80,43}(复合,len=1)——REPORT: 低层 CoderResult.isMalformed+length()==n
   （isUnmappable/isUnderflow/isOverflow 互斥）+ 便捷 decode 抛 MalformedInputException
   （getInputLength()==n，message="Input length = n"）；REPLACE: 每畸形单元一个 U+FFFD 后继续
   （"A\ufffd("、"\ufffd\ufffd"、"\ufffd"、"A\ufffdB\ufffd\ufffdC"）；IGNORE: 丢弃继续（"A("、
   ""、"ABC"）。附加形状仅断言 REPORT 长度: {80}=1、{C3}=1、{E2,82}=2、{F0,9F}=2、{C1,BF}=1、
   {F4,90,80,80}=1。
2. **US-ASCII 解码畸形语义**: 0x80 对 decoder 是 malformed（非 unmappable）len=1；三 Action +
   自定义 replacement "#" → "A#B"。
3. **endOfInput 状态机**（对照 jdk8u-dev/jdk 源码）: flush(fresh)→ISE；decode(false) 后 flush→ISE；
   decode(true) 后 decode(false)→ISE（endOfInput 升序违规）；flush 两次→UNDERFLOW；flush 后
   decode→ISE；reset() 返回 this 并复位；截断 {E4,B8}+eoi=false→UNDERFLOW 且输入 position 回退 0，
   同缓冲绝对 put 补 AD 后 eoi=true 解出"中"；{C3,28}+eoi=false 立即报 MALFORMED[1]。
4. **OVERFLOW 续读**: 容量 2 解 "hello" → OVERFLOW+"he"+in 剩 3，换大缓冲续读完成；手写小缓冲
   （2 字符=星面码点原子代理对）循环解码 == 便捷法结果。
5. **配置**: 三 Action×onMalformedInput/onUnmappableCharacter 返回 this+getter 往返；六常量默认
   replacement 均 U+FFFD；replaceWith 只校验非空且长度<=maxCharsPerByte（"é"/孤立代理对 US-ASCII
   decoder 亦接受——**不校验可编码性**，实测），""→IAE、"AB"于 UTF-16(max=1.0)→IAE；六常量
   avg/maxCharsPerByte 精确表（1/1、1/1、1/1、0.5/1、0.5/1、0.5/1）+ max>=avg>0；六常量
   isAutoDetecting=false；非自动检测 isCharsetDetected/detectedCharset→UOE。
6. **构造**: protected (Charset,float,float) 反射断言 + 子类 super(cs,0.5f,1.0f) 可实例化、
   charset()/度量往返；avg<=0/max<=0/max<avg 五组 → IAE；(1,1) 合法。
7. **ISO-8859-1 恒等映射**: 256 字节解码 → 0..255 字符；U+0000..U+00FF 编码 → 256 字节恒等。
8. **UTF-16 族畸形与补充契约**（P6 探针双侧输出一致）: UTF-16BE 奇数尾字节 {00,41,00}/{00} 与
   UTF-16LE {41,00,41} malformed len=1；孤立代理单元 {D8,00}/{00,41,DC,00} len=2 且报告前已消费
   前缀字节；各形状 REPLACE/IGNORE 输出精确断言。**BOM 变体 "UTF-16" 无 BOM 时不报错**: 2 字节
   +eoi=true 仍返回 UNDERFLOW（length() 对非错误结果→UOE）、便捷解码默认按大端解出且不插入替换符；
   {FFFE,4100} LE BOM 剥除解出 "A"。空输入低层 UNDERFLOW、便捷法解空输入长度 0；便捷法消费全部
   输入（pos==limit）且输出缓冲 positioned/可写非只读；reset() 复位编码状态但**保留 action 与
   replacement 配置**；六常量 newDecoder().charset() == 单例。
