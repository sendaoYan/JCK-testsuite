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

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 39，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `CharConversionException`：双侧同值 `-8680016352018427031L`
### 新增测试方法
1. testThrowableProtocol（全矩阵，含自禁止 initCause(this) IAE）
2. testSerializationContract（suid + 往返）
3. testPropagationAndContrast（匿名 Writer 抛 CCE 经 OutputStreamWriter 传播；UTF-8 lenient 解码不抛的对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 构造器仅 ()/(String) 两个；null message/空串区分

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
