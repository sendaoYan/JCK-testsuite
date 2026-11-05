# javax.xml.soap.Text 契约测试设计

## 1. API 功能

`javax.xml.soap.Text`（`extends javax.xml.soap.Node, org.w3c.dom.Text`，唯一自有方法 `isComment()`）统一表示 SAAJ 树中的文本与注释节点：DOM 的 Text 与 Comment 在 SAAJ 迭代器里都以本接口出现，靠 `isComment()` 区分——这是 SAAJ 相对 DOM 的一个"视图合并"设计。

## 2. 使用方式

文本通道：`SOAPElement.addTextNode("a")`（**返回元素自身**，不是 Text 节点）→ 经 `getChildElements()` 迭代取 `javax.xml.soap.Text`。注释通道：SAAJ 1.3 **没有** `addComment`，须走 DOM：`el.getOwnerDocument().createComment("c") + el.appendChild(cmt)`，之后注释同样以 SAAJ Text 出现在迭代中且 `isComment()==true`。DOM CharacterData 面（getData/setData/getLength/appendData）与 SAAJ setValue 互通。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/Text.html
- JSR 67 (SAAJ 1.3)（addComment 系 SAAJ 1.4/Jakarta 才引入）
- OpenJDK 8u RI：`com.sun.xml.internal.messaging.saaj.soap.impl.TextImpl`、`CommentImpl`
- 本仓库测试：`JDK-8/API/javax/xml/soap/Text/TextTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：addTextNode 返回值陷阱 → 相邻文本节点分离与 getValue 归并 → isComment false 矩阵 → DOM 注释通道 → addComment 缺席 → CharacterData 往返 → 空串文本 → 与 Name/QName 过滤迭代器的交互。

易错点（实测锁定）：

1. **`addTextNode` 返回 `SOAPElement`（this）**，不是新建的 Text 节点——拿 Text 必须靠迭代子节点。
2. 两次 addTextNode 生成**两个分离的 Text 节点**（不自动合并）；但元素级 `getValue()`/`getTextContent()` 读取时 RI 会把相邻文本**归并成一个节点**（normalize-on-read，同 Node 语义）——**先迭代断言 2 节点、再断言 getValue、最后断言归并成 1**。
3. **SAAJ 1.3（JDK-8）没有 `SOAPElement.addComment(String)`**（`getMethod` 抛 NoSuchMethodException）——注释要走 `createComment + appendChild`；RI 的 `CommentImpl` 实现 SAAJ Text，`isComment()==true`、value==注释数据。
4. **注释不计入元素 `getValue()`/`getTextContent()`**（二者均只算文本）。
5. 空串 addTextNode：子数 1、Text `getValue()==null`、元素 textContent 为空串。
6. SAAJ Text 上 `setValue` 与 DOM `setData` 完全等价（RI 同一存储）；`appendData` 续接。
7. `getChildElements(Name/QName)` 过滤器只返回元素子，文本节点只在无参迭代中出现。
