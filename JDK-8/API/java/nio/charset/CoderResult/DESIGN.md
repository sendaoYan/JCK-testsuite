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

### 第 4 部分增补（A3 批次：契约深化）

1. **工厂缓存**: malformed/unmappableForLength 同长度同实例（n=1/2 及 100——缓存随最大请求长度
   扩容，双侧实测）；不同长度/不同角色为不同对象；equals/hashCode 自洽；malformed≠unmappable。
2. **谓词矩阵补全**: MALFORMED[1]/UNMAPPABLE[1] toString；错误结果 isUnderflow/isOverflow 均
   false、isError 均 true、isMalformed/isUnmappable 互斥；两常量 isMalformed/isUnmappable 均
   false（补全既有 6 断言矩阵）。
3. **负长度**: malformedForLength(-1)/unmappableForLength(-1) → IAE（与 0 同）。
4. **单例交叉验证**: decode(空输入)返回 == CoderResult.UNDERFLOW；decode/encode(1 容量小输出)
   返回 == CoderResult.OVERFLOW——常量对象与编码器机制共用同一实例。
5. **异常消息**: malformedForLength(5).throwException() → MalformedInputException
   message="Input length = 5"、getInputLength()==5；unmappable(6) 同构；同一结果对象可重复
   throwException。
6. **白盒**: 所有声明构造器非 public；JDK-8 侧类**非 final**（两构造器: private+包私有）、
   JDK-27 侧 final 且单一 private 构造器——版本差异双侧各自断言。
