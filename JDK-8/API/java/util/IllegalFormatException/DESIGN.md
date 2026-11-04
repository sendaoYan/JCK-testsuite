# java.util.IllegalFormatException 测试设计案（JDK-8）

> 覆盖目标：抽象基类层次 + 全部 12 个具体子类的族内同源断言（Formatter 触发归类）。

## 1. API 功能

- 抽象 unchecked 基类 `IllegalFormatException`（继承 IllegalArgumentException），仅无参包内构造器。
- 子类家族：DuplicateFormatFlags / FormatFlagsConversionMismatch / FormatterClosed（继承 IllegalStateException）/ IllegalFormatCodePoint / IllegalFormatConversion / IllegalFormatFlags / IllegalFormatPrecision / IllegalFormatWidth / MissingFormatArgument / MissingFormatWidth / UnknownFormatConversion / UnknownFormatFlags。

## 2. 使用方式

`Class.forName("java.util.IllegalFormatException")` 断言抽象与层次；各子类用 `String.format` 非法格式串触发后 `instanceof` 归类。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/IllegalFormatException` 及子类家族。
- JDK 源码 `java/util/Formatter.java` 各 parse* 抛出点。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 抽象基类 + IllegalArgumentException 继承 | testHierarchy |
| P0 | 12 子类全部 instanceof IllegalFormatException | testSubclassFamily |
| P0 | Formatter 非法格式串触发逐类精确 | testFormatterThrowsFamily |
