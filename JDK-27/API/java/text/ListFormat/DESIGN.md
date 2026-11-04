# java.text.ListFormat 测试方案

## 1. API 功能（按方法分组）

- 工厂：`getInstance()`、`getInstance(Locale, ListFormat.Type, ListFormat.Style)`、`getInstance(String[] patterns)`、`getAvailableLocales()`。
- 查询：`getLocale()`、`getPatterns()`。
- 格式化：`format(List<String>)`、`format(Object, StringBuffer, FieldPosition)`、`formatToCharacterIterator(Object)`。
- 解析：`parse(String)`、`parseObject(String, ParsePosition)`。
- 工具：`equals`、`hashCode`、`toString`。

## 2. Java 用户如何使用

- 构造：`ListFormat f = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.SHORT);`
- 典型调用：`f.format(List.of("Apple", "Banana"))` → `"Apple & Banana"`；`f.format(List.of("Apple","Banana","Cherry"))`（FULL）→ `"Apple, Banana, and Cherry"`。
- 组合链路：构造 → `getLocale/getPatterns` → `format` → `parseObject(String, ParsePosition)` 还原列表 → `formatToCharacterIterator` 取带属性文本 → `hashCode/equals` 比较。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/ListFormat.java`：`parseObject` 通过 MessageFormat 拆列表；`toString` 输出 locale 与五个 pattern。
- JDK 测试 `test/jdk/java/text/Format/ListFormat/TestListFormat.java`、`ToStringTest.java`：覆盖 `getInstance(String[])`、`parseObject`、`formatToCharacterIterator`、`getPatterns` 不可变、空输入异常。
- 通用模式：日志/提示语中把字符串列表按 locale 规则连成 "a, b, and c"。

## 4. 测试用例设计 + 自动化实现

实现位置：`ListFormatTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：final 类、继承 `Format`、工厂/格式化/解析方法签名存在。
- 黑盒：SHORT/FULL 列表格式化、parse/parseObject 还原、formatToCharacterIterator 文本、getLocale/getPatterns、hashCode/equals/toString。
- 健壮性：固定 `Locale.US`，headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- Type×Style 3×3 pattern 表全锁定（STANDARD FULL end "{0}, and {1}"、SHORT "&"、NARROW 无连接词、UNIT/NARROW 空格连接、OR "or"）
- 元素数行为（1 原样/2 用 two/3 用 three/4 start+middle+end；空数组→IAE；null→NPE；非 String 走 toString；format(List) 与 Object[] 等价）
- 自定义 5 元 pattern（空 two/three 由 start/end 推导 "x; and y"；getLocale()=Locale.ROOT；<5 pattern→IAE；null→NPE）
- parse（parseObject 返回 List（Arrays$ArrayList 实现）idx=11；未知文本整体作单元素 err=-1；parse(String) 返回元素表）
