# java.nio.charset.spi.CharsetProvider 设计文档

## 1. API 功能

为 `Charset.forName` 等入口提供自定义字符集的 SPI 抽象类。

- 抽象方法：`Iterator<Charset> charsets()`（列出该 provider 支持的字符集）、
  `Charset charsetForName(String charsetName)`（按名查找，找不到返回 null）。
- 构造器：protected 无参构造器。

## 2. Java 用户如何使用

- 实现类：继承 `CharsetProvider`，实现 `charsets()`/`charsetForName()`，并经 `META-INF/services` 注册。
- 组合链路：`charsetForName("UTF-8")` → 返回 `Charset` 或 null；`charsets()` → 迭代全部支持项。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/spi/CharsetProvider.java`）：
  标准 provider（如 StandardCharsets、扩展 provider）均通过此 SPI 提供字符集；`Charset.forName` 遍历已安装 provider。

## 4. 测试用例设计 + 自动化实现

文件：`CharsetProviderTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / 父类 `Object` / protected 无参构造器。
- 黑盒：匿名实现 → `charsetForName` 命中/未命中返回 null → `charsets()` 迭代多个字符集 → 空 provider 行为。

## 深化追加（批次 A4，契约级）

- 白盒补充：恰好两个抽象方法（charsets/charsetForName，getDeclaredMethods 校验）；非 Serializable；
  反射实例化抽象类在模块化 JDK 上于 setAccessible 即被拒（java.base 不 open java.nio.charset.spi →
  InaccessibleObjectException）——JDK-8 侧可达 Constructor.newInstance 并断言 InstantiationException。
- 黑盒深化（计数 provider）：查询串逐字转发（lastQuery）、别名不做基类级解析、调用计数、charsets() 顺序保持、
  每次调用产出独立迭代器、空 provider、直接实例化不进全局查找表（forName/isSupported 不可见，测试不污染全局）。
- 自定义 Charset 子类契约：aliases 不可变（add → UOE）、displayName()/displayName(Locale)/toString 默认=规范名、
  canEncode 默认 true、isRegistered 的 X-/x- 前缀规则、hashCode==name.hashCode()、equals 按规范名跨实例、
  compareTo 按名、contains/newDecoder/newEncoder 委派、encode/decode 往返、null aliases 容忍、aliases() 记忆化。
- Charset 构造器负例矩阵：null 名 → NPE；""/空格/首 '-' → ICE；null 别名元素 → NPE；非法/空别名 → ICE；
  数字开头名与 A+B:C_.D-E 合法。（JDK-27 对字面量 "UTF-8"/"ISO-8859-1"/"US-ASCII" 规范名有跳过校验的
  加载器快速路径，故用中性名。）
- **JDK-27 版本特有**：Charset 构造器对别名数组做防御性拷贝（Arrays.copyOf 分支），构造后修改调用方数组不可见；
  JDK-8 不拷贝、可见（各自断言 testAliasesArrayCopySemantics）。
