# java.nio.charset.CharsetDecoder 测试方案

## 1. API 功能
- charset()/replacement()/replaceWith(String)/averageCharsPerByte()/maxCharsPerByte()
- malformedInputAction()/onMalformedInput()/unmappableCharacterAction()/onUnmappableCharacter()
- decode(ByteBuffer,CharBuffer,boolean)/flush(CharBuffer)/reset()/decode(ByteBuffer)/isAutoDetecting()/isCharsetDetected()/detectedCharset()

## 2. Java 用户如何使用
- newDecoder()->decode(in,out,true)+flush->flip 读字符; 便捷 decode(ByteBuffer); 默认 replacement U+FFFD
- 非自动检测下 isCharsetDetected()/detectedCharset() 抛 UnsupportedOperationException

## 3. 开源代码实际用法调研
- jdk8u-dev CharsetDecoder: 默认 replacement "\uFFFD"; UTF-8 解码器非自动检测; decode 尊重 remaining

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/方法存在; 黑盒: 默认值、replaceWith 往返与 null IAE、decode+flush、便捷 decode 往返(CJK/代理对)、畸形 REPORT/REPLACE、自动检测面异常。实现于 CharsetDecoderTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.

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
