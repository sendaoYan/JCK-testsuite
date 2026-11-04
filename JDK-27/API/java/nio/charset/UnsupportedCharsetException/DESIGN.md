# java.nio.charset.UnsupportedCharsetException 设计文档

## 1. API 功能

请求的字符集名称合法但不受支持（未安装对应 provider）时抛出的运行时异常。

- 构造器：`UnsupportedCharsetException(String charsetName)`（唯一构造器）。
- 继承链：`UnsupportedCharsetException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回该名称；`getMessage()` 等于该名称。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("no-such-charset")` 时 `catch (UnsupportedCharsetException e)`，
  用 `e.getCharsetName()` 取回名称。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/build/.../gensrc/java.base/java/nio/charset/UnsupportedCharsetException.java`，模板生成）：构造器 `super(String.valueOf(charsetName))`。
- `Charset.forName` 在找不到 charset 时抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`UnsupportedCharsetExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / `String` 构造器。
- 黑盒：直接 `new` → `getCharsetName`/`getMessage` 一致 → 层级 → 无 cause → 堆栈非空；
  `Charset.forName("definitely-not-a-real-charset")` 触发该异常。

## 深化追加（批次 A4，契约级）

- 构造矩阵：message==name、toString 精确、null → getCharsetName()==null 且不抛、getMessage() 为字面量 "null"、空串保留。
- forName 触发矩阵：4 个合法未知名逐个断言精确类型 + 名称保真；非法名（"bad name"/""）→ ICE 而非本异常；
  forName(null) → IllegalArgumentException；isSupported 未知合法名 false 不抛、非法名 → ICE。
- 白盒补充：唯一自有方法 getCharsetName()；Serializable。
- 序列化：serialVersionUID == 1490765524727386367L（双侧实测一致）；round-trip 名称/message/null 名称保持。
- 子类：用户子类 super(name) 委派。
