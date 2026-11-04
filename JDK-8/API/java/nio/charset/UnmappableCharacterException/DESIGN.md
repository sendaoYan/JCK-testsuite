# java.nio.charset.UnmappableCharacterException 测试方案

## 1. API 功能
- UnmappableCharacterException 继承 CharacterCodingException; UnmappableCharacterException 有 getInputLength()/getMessage() (Malformed/Unmappable), CharacterCodingException 仅无参构造

## 2. Java 用户如何使用
- 编解码 REPORT 模式下畸形/不可映射输入抛出

## 3. 开源代码实际用法调研
- jdk8u-dev: Malformed/Unmappable getMessage 返回 "Input length = N"

## 4. 测试用例设计 + 自动化实现
- 白盒: 父类/构造器/Serializable/消息; 黑盒: getInputLength/getMessage、真实抛出路径 (CoderResult.throwException)。实现于 UnmappableCharacterExceptionTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.

## 深化追加（批次 A4，契约级）

- 构造矩阵：长度 0/1/正/负/MAX/MIN 全部原样往返，getMessage 精确 `"Input length = N"`（双侧一致）；
  toString == `java.nio.charset.UnmappableCharacterException: Input length = N`；可 catch 为 IOException；受检；Serializable。
- 真实触发矩阵：US-ASCII REPORT 编码 "\u4e2d" → length 1；"a\u4e2db" 首个不可映射字符处 length 1
  （ASCII 前缀不报告）；增补字符（代理对 "\uD83D\uDE00"）→ length 2；
  CoderResult.unmappableForLength(5).throwException() → UCE(5)；Charset.encode 便捷入口 REPLACE 不抛（负空间）。
- 序列化：serialVersionUID == -7026962371537706123L（双侧实测一致）；round-trip 后 length/message/层级保持；
  负长度往返保持。
- 子类：用户子类 super(n) 委派 length 与 message 语义。
