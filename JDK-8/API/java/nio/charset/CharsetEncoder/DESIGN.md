# java.nio.charset.CharsetEncoder 测试方案

## 1. API 功能
- charset()/replacement()/replaceWith(byte[])/isLegalReplacement(byte[]); averageBytesPerChar()/maxBytesPerChar()
- malformedInputAction()/onMalformedInput()/unmappableCharacterAction()/onUnmappableCharacter()
- encode(CharBuffer,ByteBuffer,boolean)/flush(ByteBuffer)/reset()/encode(CharBuffer)/canEncode(char)/canEncode(CharSequence)

## 2. Java 用户如何使用
- newEncoder()->配置错误动作->reset()->encode(in,out,true)+flush->flip 读字节; 便捷 encode(CharBuffer)
- 默认 replacement 单字节 '?', 默认 malformed/unmappable REPORT

## 3. 开源代码实际用法调研
- jdk8u-dev CharsetEncoder: 默认 replacement '?'; reset 保留错误动作; replaceWith 校验长度<=maxBytesPerChar

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/方法存在; 黑盒: 默认值、replaceWith 往返与过长 IAE、encode+flush 低层、便捷 encode 往返、canEncode、不可映射/畸形 REPORT 与 REPLACE 语义。实现于 CharsetEncoderTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.
