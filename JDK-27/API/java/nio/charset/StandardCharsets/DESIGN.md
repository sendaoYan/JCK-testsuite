# java.nio.charset.StandardCharsets 测试方案

## 1. API 功能
- 常量: `US_ASCII`, `ISO_8859_1`, `UTF_8`, `UTF_16BE`, `UTF_16LE`, `UTF_16`, `UTF_32BE`, `UTF_32LE`, `UTF_32`
  (9 个 `public static final Charset`)

## 2. Java 用户如何使用
- 直接取常量编解码, 无需 forName: `StandardCharsets.UTF_8.encode(s)` / `.decode(bb)`.
- 组合链路: `UTF_8.encode(String) -> UTF_8.decode(ByteBuffer)` 往返; `UTF_16BE/LE/32BE.encode("A")` 核对字节布局.

## 3. 开源代码实际用法调研
- `test/jdk/tools/jlink/runtimeImage/GenerateJLIClassesTest.java`: `fileString.getBytes(StandardCharsets.UTF_8)`.
- `test/jdk/tools/jmod/JmodTest.java`: `import static java.nio.charset.StandardCharsets.UTF_8;`.
- `test/jdk/java/io/InputStreamReader/StatefulDecoderNearEOF.java`: `StandardCharsets.UTF_16.newDecoder()`.

## 4. 测试用例设计 + 自动化实现
推导: 9 常量名字与单例 (== forName)、UTF-16BE/LE/32BE/32LE 字节布局、UTF_16 BOM、UTF-8 往返
(CJK/代理对)、ISO-8859-1 vs US-ASCII 可编码性、US-ASCII 编码 CJK 抛 CharacterCodingException。
实现于 `StandardCharsetsTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 只测标准字符集; BOM 断言接受两种字节序 (不绑平台); 中文/emoji 固定字面量; 无墙钟/随机/
哈希顺序/全局状态/文件/网络/线程依赖.

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
