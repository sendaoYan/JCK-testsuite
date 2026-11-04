# java.io.UnsupportedEncodingException 测试方案 (DESIGN)

## 1. API 功能
- `(String charsetName)` / `()`：按名字请求的字符集不被支持的受检异常（String.getBytes/new String/InputStreamReader/OutputStreamWriter 的名字重载）。

## 2. Java 用户如何使用
- 旧 API 用字符串名字指定编码；拼错或环境缺字符集时捕获并回退默认编码。

## 3. 开源代码实际用法调研
- 抛出点 — `jdk25u-dev/src/java.base/share/classes/java/lang/String.java`（getBytes(String)/String(byte[],String)）与 `jdk25u-dev/src/java.base/share/classes/sun/nio/cs/StreamDecoder/StreamEncoder.java`（charsetName 查找失败）。
- 官方负例 — `jdk25u-dev/test/jdk/java/io/InputStreamReader/` 与 String 编码测试。

## 4. 测试用例设计 + 自动化实现（UnsupportedEncodingExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器（message 约定为字符集名）。
  2. 真实触发矩阵：String.getBytes(未知名) / new String(bytes, 未知名) / InputStreamReader / OutputStreamWriter，message 均为该名字。
  3. 对照：UTF-8/US-ASCII 正常往返，Charset 对象 API 不受影响。
  4. 可按 IOException 捕获；异常自身可序列化。
- 健壮性: 字符集名加纳秒后缀防止撞名，全内存。

产出：`UnsupportedEncodingExceptionTest.java`（白盒 3 + 黑盒 11 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 51，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `UnsupportedEncodingException`：双侧同值 `-4274276298326136670L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract
3. testTriggerSurface（五个 java.io 按名表面全部 message==charset 名 + 别名矩阵 + null/空名负例）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- getBytes/new String/InputStreamReader/OutputStreamWriter/PrintStream/PrintWriter/BAOS.toString 全部 message==charset 名（逐字）
- 「UTF-88」近似名 → 原样回显
- 空串名 → 空 message（非 null）
- null 名 → NullPointerException（String 两个方法 + ISR）
- utf8/UnicodeLittle/iso-8859-1↔ISO8859_1 别名族全部可用

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
