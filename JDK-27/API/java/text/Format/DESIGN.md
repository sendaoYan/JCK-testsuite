# java.text.Format — 测试设计案

## 1. API 功能
`Format` 是所有格式化类的抽象骨架（日期/数字/消息格式的公共父类）：

- 构造：`protected Format()`（仅子类可用）。
- 格式化：`final String format(Object)`（模板方法，内部新建 StringBuffer+FieldPosition 后委托抽象方法）；
  `abstract StringBuffer format(Object, StringBuffer, FieldPosition)`（追加式格式化，可回报字段位置）；
  `AttributedCharacterIterator formatToCharacterIterator(Object)`（默认实现：把 `format(obj)` 的
  文本包进无属性 AttributedString 返回迭代器；DecimalFormat/SimpleDateFormat/MessageFormat 等覆写为
  带字段属性的输出）。
- 解析：`abstract Object parseObject(String, ParsePosition)`（从 index 起解析，成功推进 index，
  失败置 errorIndex 并返回 null）；`Object parseObject(String) throws ParseException`
  （便捷方法，index==0 时抛 ParseException，errorOffset 取 errorIndex）。
- 其他：`clone()`（Object.clone 浅拷贝）、`implements Serializable, Cloneable`。

## 2. Java 用户如何使用
用户几乎不直接用 Format，而是：(a) 以 `Format` 类型持有具体子类做统一格式化/解析
（`Format f = NumberFormat.getInstance(); f.format(x); f.parseObject(s)`）；(b) **自定义子类**
实现 format/parseObject 两个抽象方法（如 Spring 的 `CustomDateEditor` 内部、各类框架的
`FormatFactory`）；(c) GUI 组件（JFormattedTextField）以 Format 做编辑器模型。
典型链路：`new MyFormat() -> format(obj, sb, fp) -> fp.begin/end -> parseObject(src, pos) ->
pos.index/errorIndex -> parseObject(src) -> ParseException.getErrorOffset()`。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/Format.java`（本地 `~/git/jdk25u-dev`）：
  `format(Object)` 的实现即 `format(obj, new StringBuffer(), new FieldPosition(0)).toString()`；
  `parseObject(String)` 在 `pos.index == 0` 时抛 `ParseException(..., pos.errorIndex)`——本测试
  第 1/4 节断言逐条对应该实现。
- OpenJDK `test/jdk/java/text/Format/common/FormatIteratorTest.java`：JDK 官方测试驱动所有
  Format 子类的 `formatToCharacterIterator`，遍历 AttributedCharacterIterator 断言 run/属性。
- OpenJDK `test/jdk/java/text/Format/CompactNumberFormat/TestFormatToCharacterIterator.java`：
  新子类覆写 `formatToCharacterIterator` 的契约样例。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/Format.html)：
  文档明确 “formatToCharacterIterator 默认抛 UnsupportedOperationException，子类应覆写”。
- Stack Overflow 关于实现 `formatToCharacterIterator` 的讨论：常见做法是构造 `AttributedString`
  并返回 `getIterator()`。

## 4. 测试用例设计 + 自动化实现（FormatTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| final format(Object) 委托 | 自定义子类 UpperFormat：返回值 == 子类写入 buffer 的文本，且子类方法被调用 |
| format(...) 追加语义 | 预置 ">" 的 buffer：返回同一 buffer 引用、内容 ">AB"、FieldPosition begin=1/end=3 |
| parseObject(String,ParsePosition) 成功 | 返回值 + index 推进 + errorIndex 保持 -1 |
| 解析失败 | 返回 null 且 errorIndex 记录位置 |
| 从中间 index 解析 | ParsePosition(3) 起点，结果 index=5 |
| parseObject(String) | 成功返回值；失败抛 ParseException 且 getErrorOffset()==errorIndex |
| clone | 副本 != 原对象、同类、仍可 format/parseObject |
| 默认 formatToCharacterIterator | 最小子类上返回无属性迭代器、文本等于 format(Object) 大写结果、bounds 匹配 |
| 真实子类链路 | DecimalFormat(Locale.US)：format(1234.5)="1,234.5"；parseObject 消费数字前缀
  停在 index 7；纯字母失败 null+errorIndex；parseObject(String) 抛 ParseException |
| 序列化 | DecimalFormat 往返后经 Format 引用 format 结果一致 |

共 8 组场景、约 25 个 check（`testBlackBox()`）。
