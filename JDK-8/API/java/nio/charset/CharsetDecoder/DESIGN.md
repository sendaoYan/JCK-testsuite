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
