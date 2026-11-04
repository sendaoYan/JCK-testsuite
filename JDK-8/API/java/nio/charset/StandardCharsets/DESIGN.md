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

### 第 4 部分增补（A3 批次：契约深化；JDK-8 侧六常量、JDK-27 侧九常量含 UTF-32 族）

1. **别名表**: UTF-8∋{UTF8,unicode-1-1-utf-8}、ISO-8859-1∋{latin1,ISO_8859_1,8859_1}、
   US-ASCII∋{ASCII,ISO646-US,ANSI_X3.4-1968}、UTF-16BE∋{UnicodeBigUnmarked,X-UTF-16BE}、
   UTF-16LE∋{UnicodeLittleUnmarked,X-UTF-16LE}、UTF-16∋{unicode,utf16,UnicodeBig}
   （集合迭代序双侧不同，仅断言成员）；aliases() 不可变（clear/add→UOE）；别名集不含规范名。
2. **别名→单例**: forName("UTF8"/"latin1"/"ascii"/"UnicodeBigUnmarked"/"UnicodeLittleUnmarked"/
   "unicode") 与对应常量 **== 同一实例**。
3. **身份**: 六常量 canEncode/isRegistered/toString==name/newDecoder+newEncoder 非空。
4. **contains 子集**: UTF-8∋US-ASCII、UTF-8∋ISO-8859-1、ISO-8859-1∋US-ASCII、US-ASCII∌UTF-8、
   UTF-16∋UTF-8、UTF-16BE∋UTF-16LE、自身∋自身（六项）。
5. **BOM 解码语义**: UTF-16 解码剥双字节序 BOM（FEFF/FFFE 输入均→"A"）；UTF-16BE 解码器保留
   U+FEFF 为字符（区别于带 BOM 变体）；UTF-8 €=E282AC；ISO-8859-1 256 字节恒等解码。
6. **白盒**: 单一 private 构造器（工具类）；JDK-8 侧断言**无 UTF_32 字段**、JDK-27 侧断言有
   并验证 UTF_32 规范名/单例/别名 UTF32/canEncode/contains 关系（版本镜像差异）。
