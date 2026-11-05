# CollapsedStringAdapter 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.adapters.CollapsedStringAdapter` 实现 `xs:token` 的 whiteSpace **collapse** 规范化：`unmarshal`（入站）把 XML 空白字符（空格、\t、\n、\r）序列折叠为单个空格并去除首尾空白；`marshal`（出站）为 no-op 恒等。用于把 `String` 属性约束为 token 词法空间。

## 2. 使用方式

```java
static class Bean {
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    String token;
}
CollapsedStringAdapter a = new CollapsedStringAdapter();
a.unmarshal("  a \t b  ");   // -> "a b"
```

本测试直接实例化并铺开边界用例矩阵。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/CollapsedStringAdapter.html
- XML Schema Part 2 xs:token / whiteSpace collapse 规范
- OpenJDK 8u RI 实现（本测试对边界行为实测锁定）
- 本仓库测试 `CollapsedStringAdapterTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 类契约：非 final（可子类化，匿名子类继承行为）、extends `XmlAdapter<String,String>`、单 public 构造器、泛型引用桥接调用。
2. `unmarshal` 正例：`null→null`、`""→""`、全空白（"   "、"\t"、"\r\n "）→`""`、`"a\tb\nc"→"a b c"`、`"\t\n x"→"x"`（首部剥离）、内部空白串折叠为一个空格、尾部剥离、已折叠串不变、幂等（collapse∘collapse==collapse）。
3. 非空白控制字符：`\u000B`（垂直制表）、`\u000C`（换页）**不是 XML 空白**，原样保留，仅其后的 `\r` 被剥掉（`"\u000Ba\u000Cc\r"→"\u000Ba\u000Cc"`）。
4. `marshal` no-op：null→null、`" a  b "` 原样、tab 原样、全空白原样、双重 marshal 仍不折叠。
5. 子类：匿名子类继承折叠行为。

易错规范细节：

- collapse 与 replace 的区别：本适配器会**剥首尾**且**串内折叠为一**；NormalizedStringAdapter 两者都不做。
- 控制台打印 `\u000B/\u000C` 不可见——断言必须用 length/charAt 数值，不能凭眼测 equals 输出。
- `marshal` 不做任何校验（出站恒等），与 `unmarshal` 不对称。
- 已折叠输入 RI 直接原对象返回（优化），但契约只保证 equals，勿断言引用相等。
