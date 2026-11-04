# java.nio.charset.CoderResult 测试方案

## 1. API 功能
- 常量 UNDERFLOW/OVERFLOW; 工厂 malformedForLength(int)/unmappableForLength(int)
- 查询 isUnderflow()/isOverflow()/isError()/isMalformed()/isUnmappable()/length(); toString(); throwException()

## 2. Java 用户如何使用
- enc.encode(in,out,true) 得 CoderResult->isUnderflow() 判定或 throwException() 转异常
- UNDERFLOW/OVERFLOW.length() 抛 UnsupportedOperationException; malformedForLength(0) 抛 IAE

## 3. 开源代码实际用法调研
- jdk8u-dev CoderResult: toString 错误追加 [length]; throwException 分派四类异常; 错误长度缓存共享

## 4. 测试用例设计 + 自动化实现
- 白盒: public/常量存在; 黑盒: 常量判定组合、toString、length 异常、throwException 四类分派、malformed/unmappable 工厂与缓存、非法长度 IAE。实现于 CoderResultTest.java

健壮性: 只测标准字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1); 中文/emoji/é/€/� 用 Unicode 字面量
固定 (源码 ASCII 安全); defaultCharset 只断言非空不绑具体值; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.
