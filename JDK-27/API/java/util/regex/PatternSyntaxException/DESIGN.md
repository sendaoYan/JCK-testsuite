# java.util.regex.PatternSyntaxException 测试方案

## 1. API 功能（按方法分组）

- 构造器：`PatternSyntaxException(String desc, String regex, int index)`。
- 查询：`getDescription()`、`getPattern()`、`getIndex()`、`getMessage()`（多行，含错误索引定位箭头 `^`）。
- 继承：`IllegalArgumentException` 的子类（可被 `catch (IllegalArgumentException)` 捕获）；可序列化。

## 2. Java 用户如何使用

- 通常不直接 new，而是捕获 `Pattern.compile(非法正则)` 抛出的实例。
- 典型调用：
  - `catch (PatternSyntaxException e) { e.getDescription(); e.getPattern(); e.getIndex(); e.getMessage(); }`
  - 向用户展示 `getMessage()`（含描述、索引、`^` 定位）。
- 真实组合链路：
  1. `Pattern.compile(regex)` 对非法正则抛 `PatternSyntaxException`。
  2. 读 `getPattern()` 回显非法正则、`getIndex()` 定位错误、`getDescription()` 描述。
  3. 直接 `new PatternSyntaxException(desc, regex, index)` 构造用于单元测试/包装。

## 3. 开源代码实际用法调研（来源明确，非记忆编造）

- JDK 源码 `java.util.regex.PatternSyntaxException.java:56-63`：构造器三个字段 `desc`/`pattern`/`index`
  直接存储；`getMessage()` 拼接 `desc` +（index>=0 时）`" near index " + index` + 换行 + pattern
  +（index 在 pattern 范围内时）换行 + 空格 + `^`。
- JDK 源码 `java.util.regex.PatternSyntaxException.java:97-106`：`getIndex()` 返回近似错误索引，
  未知时 `-1`。
- JDK 测试 `test/jdk/java/util/regex/RegExTest.java` 多处：非法正则编译用
  `assertThrows(PatternSyntaxException.class, () -> Pattern.compile(...))`。
- JDK 源码 `java.util.regex.Pattern.java:1547-1560`：构造器对未知标志抛 `IllegalArgumentException`
  （非 PatternSyntaxException），对语法错误抛 `PatternSyntaxException`，两者需区分断言。

## 4. 测试用例设计 + 自动化实现

实现位置：`PatternSyntaxExceptionTest.java`（`testWhiteBox()` 白盒 + 下列黑盒方法）。

- `testConstructor()`：直接构造后 `getDescription/getPattern/getIndex` 精确值，`getMessage()` 完整
  多行格式（`System.lineSeparator()` 拼接，含 `^` 定位）。
- `testMessageVariants()`：index=-1 省略索引与箭头；index 超长省略箭头；null pattern 保留。
- `testCompileErrors()`：`(`/`a{2,1}`/`*a`/`[` 编译抛 `PatternSyntaxException`，断言精确
  `getPattern`/`getIndex`/`getDescription`（JDK-27 实测值）。
- `testIsIllegalArgument()`：`instanceof IllegalArgumentException`、`getMessage` 含 pattern。
- 健壮性：全部固定输入；`getMessage()` 用 `System.lineSeparator()` 而非硬编码换行；无墙钟/随机/
  无序集合迭代；无全局副作用；无文件/网络/端口；headless-safe 不加 `@key`。
