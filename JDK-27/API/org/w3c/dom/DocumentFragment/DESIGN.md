# org.w3c.dom.DocumentFragment — 设计案

## 1. API 功能
org.w3c.dom.DocumentFragment 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DocumentFragment() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DocumentFragment）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentFragmentTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentFragmentTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
身份：nodeType 11、nodeName "#document-fragment"、无父、getAttributes null、空 frag 相等；混类子节点容器。
搬运工（porter）语义：appendChild(frag)/insertBefore(frag,null)/replaceChild(frag,old) 均把子节点按序搬入目标而 frag 自身清空；文档级拒绝：含 element 的 frag append 到已有 root 的 Document、以及 text-only frag → HIERARCHY_REQUEST_ERR(3) 且 frag 子节点保持原状（无半搬）；空 frag append 合法 no-op；**frag(element) append 到空 Document → 元素成为 documentElement**（实测）；fragment.normalize 合并相邻 Text；setTextContent 用单个 Text 替换全部子节点；cloneNode(true)/importNode(true) 深拷、importNode(false) 得空 frag；子树扫描经子元素 getElementsByTagName；注意 frag.appendChild(frag) 在 Xerces 会死循环——测试中不构造该调用。
