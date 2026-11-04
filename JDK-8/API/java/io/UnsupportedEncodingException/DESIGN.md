# java.io.UnsupportedEncodingException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；消息为不支持的字符集名。

## 2. Java 用户如何使用
- 使用 String.getBytes/构造、InputStreamReader/OutputStreamWriter 的按名重载时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/UnsupportedEncodingException.java`。

## 4. 测试用例设计 + 自动化实现（UnsupportedEncodingExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. String.getBytes(bogus) 抛且消息为字符集名。
3. new String(bytes, bogus) 抛。
4. InputStreamReader/OutputStreamWriter 未知字符集抛。
5. 合法名（UTF-8/US-ASCII）往返（对比）。
6. 作为 IOException 捕获 + 序列化保留消息。
- 健壮性: 固定 bogus 字符集名；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 48，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
