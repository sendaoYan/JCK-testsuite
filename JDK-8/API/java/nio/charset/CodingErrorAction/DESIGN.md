# java.nio.charset.CodingErrorAction 测试方案

## 1. API 功能
- 常量 IGNORE/REPLACE/REPORT; toString(); 私有构造器

## 2. Java 用户如何使用
- 配置 onMalformedInput/onUnmappableCharacter 指定错误处理策略

## 3. 开源代码实际用法调研
- jdk8u-dev CodingErrorAction: 单例常量, 私有构造器

## 4. 测试用例设计 + 自动化实现
- 白盒: public/私有构造/三常量; 黑盒: toString 与常量互异/自等。实现于 CodingErrorActionTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.
