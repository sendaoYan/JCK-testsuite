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
