# java.util.regex.PatternSyntaxException 测试设计案（JDK-8）

> 覆盖目标：继承层次 + 构造器字段 + 精确 getMessage() 格式 + JDK-8 编译错误精确值 + 序列化，非冒烟。

## 1. API 提供了哪些功能

- 构造器：`PatternSyntaxException(String desc, String regex, int index)`。
- 查询：`getDescription()`、`getPattern()`、`getIndex()`、`getMessage()`（多行，含 `^` 定位）。
- 继承：`IllegalArgumentException` 子类；实现 `Serializable`（serialVersionUID == -3864639126226059218L）；
  非 final。

## 2. Java 用户如何使用

- 通常不直接 new，而是捕获 `Pattern.compile(非法正则)` 抛出的实例。
- `getMessage()` 展示描述 + 近似索引 + 正则 + `^` 定位。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/util/regex/PatternSyntaxException.java`
  （getMessage 精确格式：`desc[ near index N]` + 换行 + pattern +（index 在范围内时）换行 + index 个空格 + `^`）。
- JDK-27 同目录 DESIGN.md（getMessage 格式与 JDK-8 一致）。

## 4. 测试用例设计（映射到 PatternSyntaxExceptionTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非 final、继承 IAE、Serializable、构造器 + 方法形态 | testWhiteBox |
| P0 | 直接构造 + 精确 getMessage 完整格式 | testConstructor |
| P0 | index=-1 省略索引/箭头；index 超长省略箭头；null pattern 保留 | testMessageVariants |
| P0 | JDK-8 编译错误精确值：`(`→idx1/Unclosed group、`a{2,1}`→idx5/Illegal repetition range、`*a`→idx0/Dangling meta character、`[`→idx0/Unclosed character class、重复命名组 | testCompileErrors |
| P0 | instanceof IllegalArgumentException；getMessage 含 pattern | testIsIllegalArgument |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |

## 关键规范细节（易错，已锁入断言）

- getMessage 用 `System.lineSeparator()` 拼接，不硬编码 `\n`。
- 当 `index<0` 或 `pattern==null` 或 `index>=pattern.length()` 时省略 `^` 定位行。
- 重复命名组 JDK-8 报 `Named capturing group <n> is already defined`（index 11）。

## 注意事项

- 固定输入；无默认 Locale/墙钟/随机；headless-safe 不加 `@key`。
