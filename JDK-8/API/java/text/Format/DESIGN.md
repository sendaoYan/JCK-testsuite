# java.text.Format 测试设计案（JDK-8）

> 覆盖目标：完整契约（抽象骨架、final format 委托、parseObject 双形态、clone、formatToCharacterIterator
> 默认实现、Serializable），非冒烟。

## 1. API 提供了哪些功能

- 构造：`protected Format()`。
- 格式化：`final format(Object)`、`abstract format(Object,StringBuffer,FieldPosition)`、
  `formatToCharacterIterator(Object)`（默认无属性包装）。
- 解析：`abstract parseObject(String,ParsePosition)`、`parseObject(String) throws ParseException`。
- 契约：`clone()`；`implements Serializable, Cloneable`。

## 2. Java 用户如何使用

- 以 `Format` 引用持有具体子类统一格式化/解析；自定义子类实现 format/parseObject 两个抽象方法。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/Format.java`。
- JDK-27 同目录 DESIGN.md（JDK-8 的 formatToCharacterIterator 默认实现一致）。

## 4. 测试用例设计（映射到 FormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Serializable/Cloneable、protected 构造器、方法签名 | testWhiteBox |
| P0 | final format(Object) 委托 + format(...) 追加语义 + FieldPosition | testBlackBox |
| P0 | parseObject(String,ParsePosition) 成功/失败/续解析 | testBlackBox |
| P0 | parseObject(String) 成功/失败抛 ParseException | testBlackBox |
| P0 | clone 独立副本 | testBlackBox |
| P0 | 默认 formatToCharacterIterator 无属性包装 | testBlackBox |
| P0 | DecimalFormat 真实子类链路 + 序列化往返 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `format(Object)` 是 final 模板方法；`parseObject(String)` 失败时 `getErrorOffset()==errorIndex`。
- 默认 `formatToCharacterIterator` 把 `format(obj)` 包成无属性 AttributedString 迭代器。

## 注意事项

- 固定 Locale.US；自定义子类（UpperFormat）避免依赖具体 format 类；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 自定义子类模板方法链（format(Object, StringBuffer, FieldPosition) 委托、formatToCharacterIterator、parseObject 双参/单参）
- parseObject 返回 null → ParseException（errorOffset）
- clone/equals；抽象契约（不可实例化）
