# java.text.BreakIterator 测试方案

## 1. API 功能（按方法分组）

- 常量：`DONE=-1`。
- 工厂：`getWordInstance`/`getLineInstance`/`getCharacterInstance`/`getSentenceInstance` 各 0/1 参重载、`getAvailableLocales()`。
- 导航：抽象 `first`/`last`/`next(int)`/`next`/`previous`/`following`、具体 `preceding`/`isBoundary`、抽象 `current`。
- 文本：抽象 `getText`、`setText(String)`、抽象 `setText(CharacterIterator)`。
- 契约：`clone`（实现 Cloneable）。

## 2. Java 用户如何使用

- 工厂实例化：`BreakIterator.getWordInstance(Locale.US)` 获得分词器。
- 典型调用：`bi.setText("hello world")` 后 `for (int i = bi.first(); i != BreakIterator.DONE; i = bi.next())` 遍历边界；`bi.following(offset)`/`bi.preceding(offset)`/`bi.isBoundary(offset)` 定位边界。
- 组合链路：工厂实例化 → `setText` → `first`/`next` 遍历 → `last`/`previous` 反向 → `following`/`preceding`/`isBoundary` 查询 → `getText`/`clone`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.BreakIterator` 工厂委托 ICU/`sun.text` 的边界引擎（字符/词/句/行）。
- JDK 测试 `test/jdk/java/text/BreakIterator/*.java`：断言各 locale 的字符/词/句边界。
- 通用模式：文本编辑器换行（line instance）、搜索高亮分词（word instance）、句级分段（sentence instance）。

## 4. 测试用例设计 + 自动化实现

实现位置：`BreakIteratorTest.java`（`testWhiteBox()` 白盒 + 六个黑盒方法）。

- 正常链路：字符实例 `"abc"` 边界 [0,1,2,3]、组合重音 `"á"` 单字素 [0,2]；词实例 `"hello world"` [0,5,6,11]；句实例 `"Hello. World."` [0,7,13]；行实例 `"hello world"` [0,6,11]。
- 导航链路：`first`/`next`/`last`/`previous`/`following`/`preceding`/`isBoundary`/`current`/`next(int)` 的组合定位。
- 文本/克隆链路：`getText` 返回 CharacterIterator、`clone` 产生相同边界。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。
