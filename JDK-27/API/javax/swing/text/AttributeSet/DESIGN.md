# javax.swing.text.AttributeSet — DESIGN

## 1. API 功能
文本属性集合的只读接口（属性名 → 属性值的键值集合，支持通过 resolve parent 向上解析）。核心方法：
- 常量：`NameAttribute`、`ResolveAttribute`（均委托到 `StyleConstants` 同名常量）。
- 查询：`getAttributeCount()`、`isDefined(Object)`、`getAttribute(Object)`、`getAttributeNames()`（返回 `Enumeration`）。
- 包含判断：`containsAttribute(Object name, Object value)`、`containsAttributes(AttributeSet)`。
- 比较/复制：`isEqual(AttributeSet)`、`copyAttributes()`。
- 解析父级：`getResolveParent()`。
- 嵌套标记接口（空接口，仅作类型签名）：`CharacterAttribute`、`ColorAttribute`、`FontAttribute`、`ParagraphAttribute`。

## 2. Java 用户如何使用
用户通常不直接实现该接口，而是持有 `AttributeSet` 类型引用读取属性：
```java
AttributeSet a = someElement.getAttributes();
if (a.isDefined(StyleConstants.Bold)) { boolean b = StyleConstants.isBold(a); }
Enumeration<?> names = a.getAttributeNames();
AttributeSet parent = a.getResolveParent();
```
`NameAttribute`/`ResolveAttribute` 作为保留键：`NameAttribute` 存集合名，`ResolveAttribute` 存解析父集。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AttributeSet.java`：`NameAttribute = StyleConstants.NameAttribute`、`ResolveAttribute = StyleConstants.ResolveAttribute`，四个嵌套标记接口为空接口。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`SmallAttributeSet` 直接 `implements AttributeSet`，实现 `getAttribute` 时先查本地，再 `getResolveParent().getAttribute(key)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/SimpleAttributeSet.java`：`getAttribute` 同样的 parent fall-through 逻辑。

## 4. 测试用例设计 + 自动化实现（`AttributeSetTest.testBlackBox`）
1. 反射断言 `NameAttribute`/`ResolveAttribute` 是 public static final，且与 `StyleConstants` 同名常量恒等（==）。
2. 用 `SimpleAttributeSet.EMPTY` 作为 `AttributeSet` 引用，验证空集合语义：count==0、names 空、resolve parent null、getAttribute 返回 null、isDefined false、isEqual/containsAttributes 自反成立。
3. 四个嵌套标记接口均为接口且 `getDeclaredMethods().length == 0`（空标记）。

## 5. 契约深化（本轮追加）
`testTypeShape/testWellKnownKeys/testCrossImplementationEquals/testContractThroughImplementations`（59 断言）：
- 接口完整方法面（15 个方法）与常量键：NameAttribute/ResolveAttribute/KeyEndOffset/KeyStartOffset 等互异且多为 String/Integer 常量。
- 跨实现 isEqual：SimpleAttributeSet 与 StyleContext.addAttributes 产物、SimpleAttributeSet.EMPTY 内容相等即 isEqual 成立（实现无关）。
- 通过 SimpleAttributeSet/NamedStyle 双实现走完整读写契约（含 resolve parent 穿透、copyAttributes 快照独立）。
- 反例：接口本身无 contains/EMPTY（那是 AbstractElement/SimpleAttributeSet 的成员）——用反射锁定，防止误记。
