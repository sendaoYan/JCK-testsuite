# java.util.jar.Attributes 测试方案

白盒结构断言在 `AttributesTest.testWhiteBox()`，黑盒行为断言在 `AttributesTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `AttributesTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`Attributes()`（默认大小）、`Attributes(int size)`（初始容量）、`Attributes(Attributes)`（拷贝）。
- **查询**：`get(Object)`、`getValue(String)`、`getValue(Name)`、`containsKey(Object)`、`containsValue(Object)`、`size()`、`isEmpty()`。
- **修改**：`put(Object,Object)`、`putValue(String,String)`、`putAll(Map)`、`remove(Object)`、`clear()`。
- **视图/工具**：`keySet()`、`values()`、`entrySet()`、`equals`、`hashCode`、`clone`、`toString`。
- 内部为 `LinkedHashMap`（可预测迭代顺序）；属性名大小写不敏感、仅限 `[0-9a-zA-Z_-]` 且长度 1..70。

## 2. Java 用户如何使用

- 构造：`new Attributes()`；`new Attributes(16)`；`new Attributes(existing)`。
- 典型调用：`put(Name.MANIFEST_VERSION, "1.0")`、`putValue("Foo","bar")`、`getValue("foo")`（大小写不敏感）。
- 真实组合链路：`put → getValue → containsKey → containsValue → size → remove → isEmpty`；
  `putValue("Foo","bar")` 后 `putValue("foo","baz")` 替换同一键（大小写折叠）。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/Manifest/WriteBinaryStructure.java:45-47`：`Manifest mf = new Manifest(); mf.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0"); mf.getMainAttributes().put(new Name("Key"), "Value");` —— 用 `put(Name,...)` 写主属性。
- `test/jdk/java/util/jar/Attributes/TestAttrsNL.java:71`：`test(new Manifest(new ByteArrayInputStream(manifestStr.getBytes(UTF_8))), ...)` —— 通过 Manifest 读入后取 Attributes。
- `src/java.base/share/classes/java/util/jar/Manifest.java:116`：`attr.putAll(man.getMainAttributes());` —— `putAll(Attributes)` 拷贝主属性。
- `src/java.base/share/classes/java/util/jar/Attributes.java:119-121`：`getValue(String)` 定义为 `(String)get(Name.of(name))` —— 大小写不敏感来源。

## 4. 测试用例设计 + 自动化实现

- **正常**：空构造为空；`put(Name)`/`getValue(Name)`/`getValue(String)` 往返；`containsKey`/`containsValue`/`keySet`/`values`。
- **组合/大小写**：`putValue("Foo")` 后 `getValue("foo"/"FOO")` 均命中；重复 `putValue("X-Header")`/`putValue("x-header")` 折叠为单键且后者胜出。
- **视图**：`entrySet` 键排序断言内容；`values`/`keySet` 大小与内容。
- **拷贝/克隆/相等**：拷贝构造独立；`clone()` 相等且互不影响；`equals`/`hashCode` 一致。
- **边界/异常**：`putAll(普通 Map)`、`put(String key,...)`、`put(Name, 非 String)` 抛 `ClassCastException`；空名/非法字符/71 长名/`null` 抛 `IllegalArgumentException` 或 `NullPointerException`。
- **自动化**：`AttributesTest.testBlackBox()` 内 `testBasics/testCaseInsensitive/testMapViews/testCopyCloneEquality/testPutAllAndTypeChecks/testInvalidNames`。
- **健壮性**：无墙钟/随机；`entrySet` 先排序再断言；不改全局状态；无文件/网络/线程。
