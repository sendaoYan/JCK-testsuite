# java.io.CharConversionException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：字符转换失败（旧式字节↔字符转换器）的受检异常，extends IOException。

## 2. Java 用户如何使用
- 旧字符流 API 的 throws 列表中出现；现代 nio 用 MalformedInputException（CharacterCodingException 子类）替代，本类保留用于兼容与自定义转换层。

## 3. 开源代码实际用法调研
- 类型与构造 — `jdk25u-dev/src/java.base/share/classes/java/io/CharConversionException.java`。
- 现代解码失败路径走 nio charset — `jdk25u-dev/src/java.base/share/classes/sun/nio/cs/StreamDecoder.java`（对照）。

## 4. 测试用例设计 + 自动化实现（CharConversionExceptionTest.java）
- 白盒: extends IOException、两构造、非抽象。
- 黑盒场景:
  1. 构造器 message（no-arg null）。
  2. 可按 IOException 捕获。
  3. 从 Reader 方法签名（throws IOException）传播并被还原为 CCE+message。
  4. 异常自身可序列化且 message 保留。
  5. 对照：合法 UTF-8 解码正常完成。
- 健壮性: 全内存，无全局状态。

产出：`CharConversionExceptionTest.java`（白盒 4 + 黑盒 7 断言）。
