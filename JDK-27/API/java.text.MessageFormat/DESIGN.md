# java.text.MessageFormat 测试方案

## 1. API 功能（按方法分组）

- 构造器：`MessageFormat(String)`、`MessageFormat(String,Locale)`。
- 模式/区域：`applyPattern`、`toPattern`、`setLocale`、`getLocale`。
- 子格式注入：`setFormatsByArgumentIndex(Format[])`、`setFormats(Format[])`、`setFormatByArgumentIndex(int,Format)`、`setFormat(int,Format)`、`getFormatsByArgumentIndex()`、`getFormats()`。
- 格式化：`format(Object[],StringBuffer,FieldPosition)`、静态 `format(String,Object...)`、`format(Object,...)`、`formatToCharacterIterator`。
- 解析：`parse(String,ParsePosition)`、`parse(String)`（抛 ParseException）、`parseObject`。
- 契约：`clone`、`equals`、`hashCode`、`toString`。

## 2. Java 用户如何使用

- 构造：`new MessageFormat("Hello {0}, you have {1} messages.", Locale.US)`。
- 典型调用：`mf.format(new Object[]{"World", 3})`；静态 `MessageFormat.format("x={0}", 1)`。
- 子格式：`{0,number,#.##}`、`{0,choice,0#zero|1#one|2#many}`、`{0,date,short}`、`{0,time}`。
- 组合链路：构造 pattern → `setFormatByArgumentIndex`/`setFormatsByArgumentIndex` 注入 NumberFormat/ChoiceFormat/SimpleDateFormat → `format` 组合输出 → `parse` 反向还原 → `applyPattern` 换模板 → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.util.logging.LocalizedMessage`、`sun.security.util.ResourcesMgr` 使用 MessageFormat 做本地化消息模板。
- JDK 测试 `test/jdk/java/text/Format/MessageFormat/*.java`：大量占位符/子格式/parse 断言。
- Spring `org.springframework.context.MessageSource`（resource bundle 消息格式化）底层依赖 MessageFormat。
- 通用 i18n 模式：`MessageFormat.format("Disk {0} contains {1} files", dir, n)`。

## 4. 测试用例设计 + 自动化实现

实现位置：`MessageFormatTest.java`（`testWhiteBox()` 白盒 + 六个黑盒方法）。

- 正常链路：基本占位替换、静态 `format`、`format(Object[],StringBuffer,FieldPosition)` 重载。
- 子格式链路：`{0,number,#.##}` 数字子格式、`{0,choice,...}` ChoiceFormat 联动（0/1/2/5 分支）。
- 注入链路：`setFormatByArgumentIndex` 固定 SimpleDateFormat（UTC）保证日期确定性、`setFormatsByArgumentIndex` 注入三种 Format、`getFormatsByArgumentIndex` 回读。
- 解析链路：`parse` 还原占位值（Long/Double 类型）。
- 模式/区域链路：`applyPattern`/`toPattern`、`setLocale`/`getLocale`。
- 契约：`formatToCharacterIterator` 文本、`clone`/`equals`/`hashCode`。
- 健壮性：固定 Locale.US、日期用固定 Date(0)+UTC 时区的显式 SimpleDateFormat（不用默认时区的 {time} 子格式，避免跨机差异）。headless-safe，不加 `@key`。
