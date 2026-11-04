# java.text.CollationKey — 测试设计案

## 1. API 功能
`CollationKey` 表示一个字符串在特定 `Collator` 下的**预计算排序键**：把多次比较从 O(n·collation)
降为字节序比较，适合大列表排序。

- 构造：`protected CollationKey(String source)`（用户经 `Collator.getCollationKey(String)` 获得）。
- 查询：`String getSourceString()`（原始文本）。
- 比较：`abstract int compareTo(CollationKey)`（实现 Comparable）；`compareTo(Object)` 桥接方法。
- 序列化：`abstract byte[] toByteArray()`（与 compareTo 一致的字节序形式）。

## 2. Java 用户如何使用
`Collator col = Collator.getInstance(Locale.ENGLISH); CollationKey k1 =
col.getCollationKey(s1); ... Collections.sort(listOfKeys);` 对大量字符串先一次性取 key，
再用 key 排序/做 TreeMap 键；`toByteArray()` 用于把排序键存进数据库/文件跨进程比较。
链路：getCollationKey -> compareTo/Comparable.sort -> getSourceString 取回原文 -> toByteArray
字节比较/持久化。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/CollationKey.java`（本地 `~/git/jdk25u-dev`）：
  类 javadoc 给出 “repeated comparison” 的推荐用法（先取 key 再比较），并说明 toByteArray
  支持序列化比较。
- OpenJDK `src/java.base/share/classes/java/text/RuleBasedCollationKey.java`：
  `compareTo` 先 cast 成 RuleBasedCollationKey（否则 ClassCastException）、按内部字节比较并
  把结果折叠为 LESS/EQUAL/GREATER——第 7/5 节分别验证。
- OpenJDK `test/jdk/java/text/Collator/CollatorAPITest.java`、`test/jdk/java/text/Collator/Test.java`：
  官方测试调用 `getCollationKey(...)` 比较 `key1.compareTo(key2)` 与 `collator.compare` 一致。
- Apache Lucene / ICU（`CollationKey` 同源设计的 ICU `CollationKey`）：索引排序时把 key 字节
  化存入索引，是 toByteArray 的代表性开源场景。

## 4. 测试用例设计 + 自动化实现（CollationKeyTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| getSourceString | "abc"/"" 原文往返 |
| compareTo 契约 | a<b/b>a 反对称、a<c 传递、自比 0、与 Collator.compare 同号 |
| 相等文本 | 两次 getCollationKey("abc") 互比 0 |
| Comparable 排序 | 4 个 key sort 后按字母序（alpha<bravo<charlie<delta） |
| toByteArray | 非空、等文本字节相同、异文本字节不同 |
| 受保护构造+子类 | 自定义 MyKey：super(source) 可用、getSourceString/自有 compareTo/toByteArray 生效 |
| 拒绝外来实现 | RuleBased key 与 MyKey 比较抛 ClassCastException |
| collator 独立性 | 两个 English collator 的 key 顺序一致 |

共 8 组场景、约 17 个 check（`testBlackBox()`）。
