# java.io.CharConversionException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException（受检）；表示字符解码失败。

## 2. Java 用户如何使用
- 在 Reader/InputStreamReader 解码失败时以 IOException 捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/CharConversionException.java`。

## 4. 测试用例设计 + 自动化实现（CharConversionExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息/默认 null、无 cause。
2. 作为 IOException 捕获。
3. Reader 签名抛出的解码失败原样传播。
4. 异常自身序列化保留消息。
5. 合法解码路径正常完成（对比）。
- 健壮性: 显式 UTF-8；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 35，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
