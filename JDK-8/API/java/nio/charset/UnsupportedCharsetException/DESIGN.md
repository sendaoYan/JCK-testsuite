# java.nio.charset.UnsupportedCharsetException 测试方案

## 1. API 功能

字符集名称**语法合法但不受支持**（无已安装 provider 提供该名）时抛出的运行时异常。

- 构造器：`UnsupportedCharsetException(String charsetName)`（唯一构造器），`super(String.valueOf(charsetName))`。
- 继承链：`UnsupportedCharsetException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回请求名称（null 入参时返回 null，不抛）；`getMessage()` 与名称一致
  （null 入参时为字面量 `"null"`）。
- Serializable（经 RuntimeException），`serialVersionUID == 1490765524727386367L`（实测 JDK-8 与 JDK-27 一致）。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("no-such-charset")` 时 `catch (UnsupportedCharsetException e)`，
  用 `e.getCharsetName()` 取回名称。
- 与 `IllegalCharsetNameException` 的分界：语法非法（空串/非法字符/非法首字符）抛 ICE；`forName(null)`
  抛普通 `IllegalArgumentException`；语法合法但未知才抛本异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk8u-dev/jdk/src/share/classes/java/nio/charset/UnsupportedCharsetException.java`）：
  构造器 `super(String.valueOf(charsetName))`。
- 触发点：`~/git/jdk8u-dev/.../Charset.java` 的 `lookup2` 找不到字符集时 `throw new UnsupportedCharsetException(charsetName)`。
- 回归参考：`~/git/jdk/test/jdk/java/nio/charset/Charset/` 对未知名查找的思路。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`UnsupportedCharsetExceptionTest.java`（testUnsupportedCharsetException 保留 + testWhiteBox +
testCtorMatrix + testForNameTriggers + testSerialization + testSubclass）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / String 构造器 / Serializable /
  唯一自有方法 `getCharsetName()`。
- 构造矩阵：名称精确往返、message==name、toString 精确、null → getCharsetName()==null 且不抛、
  message 为字面量 `"null"`、空串保留。
- 真实触发：`Charset.forName` 对 4 个合法未知名断言精确类型 + 名称保真；非法名（"bad name"/""）断言为
  ICE 而非本异常；`forName(null)` → IllegalArgumentException；`isSupported` 未知合法名 false 不抛、
  非法名 → ICE。
- 序列化：suid 精确断言 + round-trip 名称/message/null 名称保持。
- 子类：用户子类 super(name) 委派。

健壮性：样本名均为确定性常量；无文件/网络/线程/墙钟/随机/Locale 依赖。
