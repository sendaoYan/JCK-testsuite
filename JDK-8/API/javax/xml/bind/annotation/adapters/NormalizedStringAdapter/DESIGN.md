# NormalizedStringAdapter 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.adapters.NormalizedStringAdapter` 实现 `xs:normalizedString` 的 whiteSpace **replace** 规范化：`unmarshal`（入站）把每个 `\t`、`\r`、`\n` 替换为单个空格，但**不折叠、不剥首尾**；`marshal`（出站）为 no-op 恒等。

## 2. 使用方式

```java
NormalizedStringAdapter a = new NormalizedStringAdapter();
a.unmarshal("a\tb");     // -> "a b"
a.unmarshal("\t\r\n");   // -> "   "（三个空格，不折叠）
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/NormalizedStringAdapter.html
- XML Schema Part 2 xs:normalizedString / whiteSpace replace
- OpenJDK 8u RI 实现（实测锁定）；本仓库测试 `NormalizedStringAdapterTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 类契约：**final**（与 CollapsedStringAdapter 的非 final 对比锁定）、extends `XmlAdapter<String,String>`、单 public 构造器、泛型引用桥接调用。
2. `unmarshal` 正例：null→null、`""→""`、三种空白字符各自替换为一个空格、`"\t\r\n"→"   "`（长度保持 3，无折叠）、既有空格不动（"a  b" 原样）、混合空白串 `"a \t\r\n b"→"a     b"`（5 个空格，长度保持）、首尾空白替换但不剥离（`"\ta\n"→" a "`）、干净串不变、幂等（`unmarshal(unmarshal(x)).equals(unmarshal(x))`）。
3. 非空白控制字符：`\u000B/\u000C` 保留，仅 `\r` 被替换为空格（`"\u000Ba\u000Cc\r"→"\u000Ba\u000C "`，长度 5）。
4. `marshal` no-op：null→null、tab/CR/LF/空格全原样、双重 marshal 仍不规范化。

易错规范细节：

- replace **不折叠**：tab+CR 串变成两个空格，不是 collapse 的一个——幂等断言要用"结果等于一次结果"而非想当然的"串内单空格"。
- 首尾空白被替换为空格但**不删除**（与 collapse 的关键差异，`"\ta\n"→" a "`）。
- `\u000B/\u000C` 不属于 XML 空白集合 {0x20,0x9,0xA,0xD}。
- 控制台打印不可见字符，断言用 charAt 数值。
