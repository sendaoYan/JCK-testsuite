# java.nio.charset.IllegalCharsetNameException 测试方案

## 1. API 功能

字符集名称**语法非法**（空串、含空格/NUL/`#`/`/`/`?`、首字符为 `-`/`.`/`:`/`_`、含非 ASCII 控制字符等）时抛出的运行时异常。

- 构造器：`IllegalCharsetNameException(String charsetName)`（唯一构造器），`super(String.valueOf(charsetName))`。
- 继承链：`IllegalCharsetNameException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回非法名称（null 入参时返回 null，不抛）；`getMessage()` 与名称一致
  （null 入参时为字面量 `"null"`，String.valueOf 语义）。
- Serializable（经 RuntimeException），`serialVersionUID == 1457525358470002989L`（实测 JDK-8 与 JDK-27 一致）。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("bad name")` / `Charset.isSupported("bad name")` 时
  `catch (IllegalCharsetNameException e)`，用 `e.getCharsetName()` 取回非法名称。
- 与 `UnsupportedCharsetException` 的分界：名称**语法合法但未知**（如 `1ABC`、`A+B`——数字可作首字符、
  `+` 合法）抛 UnsupportedCharsetException；`forName(null)` 抛普通 `IllegalArgumentException`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk8u-dev/jdk/src/share/classes/java/nio/charset/IllegalCharsetNameException.java`）：
  构造器 `super(String.valueOf(charsetName))`。
- 名称合法性规则：`~/git/jdk8u-dev/.../Charset.java` 的 `checkName(String)`——首字符须字母/数字，
  其后允许字母/数字/`-`/`:`/`_`/`.`；空串非法。
- 回归参考：`~/git/jdk/test/jdk/java/nio/charset/Charset/` 对 forName 非法名矩阵的思路。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`IllegalCharsetNameExceptionTest.java`（testIllegalCharsetNameException 保留 + testWhiteBox +
testCtorMatrix + testForNameTriggers + testSerialization + testSubclass）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / String 构造器 / Serializable /
  唯一自有方法 `getCharsetName()`（getDeclaredMethods 校验）。
- 构造矩阵：名称精确往返、message==name、toString 精确、null → getCharsetName()==null 且不抛、
  message 为字面量 `"null"`、空串保留。
- 真实触发（黑盒实证）：`Charset.forName` 11 个非法名逐个断言精确类型 + `getCharsetName()` 保真；
  合法但未知名（1ABC/A+B/no-such-charset-xyz）断言为 UnsupportedCharsetException；`forName(null)` →
  IllegalArgumentException；`isSupported(null)` → IAE、`isSupported("")`/`isSupported("bad name")` → ICE
  （不是返回 false——实测确认）、未知合法名返回 false 不抛。
- 序列化：suid 精确断言 + round-trip 名称/message/null 名称保持。
- 子类：用户子类 super(name) 委派与 instanceof 链。

健壮性：非法名样本为确定性常量（含 NUL、0x80 以 `new String(char[])` 构造避免源码字面量问题）；
无文件/网络/线程/墙钟/随机/Locale 依赖。
