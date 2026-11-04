# java.nio.charset.IllegalCharsetNameException 设计文档

## 1. API 功能

字符集名称格式非法（含空格等非法字符）时抛出的运行时异常。

- 构造器：`IllegalCharsetNameException(String charsetName)`（唯一构造器）。
- 继承链：`IllegalCharsetNameException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回非法名称；`getMessage()` 等于该名称。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("bad name!")` 时 `catch (IllegalCharsetNameException e)`，用 `e.getCharsetName()` 取回非法名称。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/build/.../gensrc/java.base/java/nio/charset/IllegalCharsetNameException.java`，模板生成）：构造器 `super(String.valueOf(charsetName))`，故 message 与 charsetName 一致。
- `Charset.forName` 在校验名称合法性失败时抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`IllegalCharsetNameExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / `String` 构造器。
- 黑盒：直接 `new` → `getCharsetName`/`getMessage` 一致 → 层级 → 无 cause → 堆栈非空；
  `Charset.forName("bad name!")` 触发该异常并保留名称。

## 深化追加（批次 A4，契约级）

- 构造矩阵：message==name、toString 精确、null → getCharsetName()==null 且不抛、getMessage() 为字面量 "null"
  （String.valueOf 语义）、空串保留。
- forName/isSupport 触发矩阵（11 个非法名：空串/含空格/内嵌 NUL/首字符 -.:_/含 # ? //非 ASCII 字节）逐个断言
  精确类型 + getCharsetName() 保真；合法但未知名（1ABC、A+B——数字可作首字符、+ 合法）→ UnsupportedCharsetException；
  forName(null)/isSupported(null) → IllegalArgumentException；isSupported("")/isSupported("bad name") → ICE
  （不是返回 false）；未知合法名 isSupported 返回 false 不抛。
- 白盒补充：唯一自有方法 getCharsetName()（getDeclaredMethods）；Serializable。
- 序列化：serialVersionUID == 1457525358470002989L（双侧实测一致）；round-trip 名称/message/null 名称保持。
- 子类：用户子类 super(name) 委派。
