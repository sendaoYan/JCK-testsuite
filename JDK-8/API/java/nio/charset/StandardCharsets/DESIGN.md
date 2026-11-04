# java.nio.charset.StandardCharsets 测试方案

## 1. API 功能
- 常量: US_ASCII/ISO_8859_1/UTF_8/UTF_16BE/UTF_16LE/UTF_16 (JDK-8 六常量, 无 UTF_32*)

## 2. Java 用户如何使用
- 直接取常量编码解码免 forName; UTF_16BE/LE 字节布局固定; UTF_16 带 BOM

## 3. 开源代码实际用法调研
- jdk8u-dev StandardCharsets: 六常量; 每个常量 == Charset.forName(规范名)

## 4. 测试用例设计 + 自动化实现
- 白盒: final/六字段 public static final Charset; 黑盒: 名字与 forName 单例一致、UTF-16BE/LE/UTF_16 字节布局、UTF-8 往返(CJK/emoji)、US-ASCII 不可映射替换 '?'。实现于 StandardCharsetsTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.
