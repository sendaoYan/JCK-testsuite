# java.nio.charset.CharacterCodingException 测试方案

## 1. API 功能
- CharacterCodingException 继承 IOException; CharacterCodingException 有 getInputLength()/getMessage() (Malformed/Unmappable), CharacterCodingException 仅无参构造

## 2. Java 用户如何使用
- 编解码 REPORT 模式下畸形/不可映射输入抛出

## 3. 开源代码实际用法调研
- jdk8u-dev: Malformed/Unmappable getMessage 返回 "Input length = N"

## 4. 测试用例设计 + 自动化实现
- 白盒: 父类/构造器/Serializable/消息; 黑盒: getInputLength/getMessage、真实抛出路径 (CoderResult.throwException)。实现于 CharacterCodingExceptionTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.

## 深化追加（批次 A4，契约级）

- 家族层级：MIE/UCE 均为 CCE 子类，反向不成立；CCE 可赋值给 IOException/Exception（受检）、Serializable。
- Throwable 状态契约：唯一无参构造 message/getLocalizedMessage 均 null、toString 无消息后缀；
  无 cause 构造 → initCause 返回 this、二次 initCause → IllegalStateException；addSuppressed/getSuppressed 有效。
- 真实触发链：decoder REPORT 解码 {0xC3} 抛出的运行时类型是 MalformedInputException（getInputLength==1），
  catch (CharacterCodingException) 与 catch (IOException) 均命中；encoder REPORT 编码 "\u4e2d" 抛 UCE。
  反向空间：Charset.encode/decode 便捷入口默认 REPLACE，永不抛本异常。
- 序列化：serialVersionUID == 8421532232154627783L（实测 JDK-8/JDK-27 一致）；round-trip 后
  message/cause/层级保持；initCause 附加的 cause 往返保持。
