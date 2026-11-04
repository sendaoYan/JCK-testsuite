# javax.swing.text.html.AccessibleHTML — DESIGN

## 1. API 功能
HTML 可访问性桥（包私有 `class AccessibleHTML implements Accessible`），为 `JEditorPane` 的 HTML 内容提供辅助访问树。核心成员：
- 构造器：`AccessibleHTML(JEditorPane)`（包私有）。
- `getAccessibleContext()`：返回 `RootHTMLAccessibleContext`（内部类，包私有）。
- 嵌套类：`HTMLAccessibleContext`、`IconElementInfo`、`TableElementInfo`、`TextElementInfo` 等，把文档元素映射为 Accessible 角色/状态/文本。
- 通常经 `JEditorPane.getAccessibleContext()` 的 `AccessibleJEditorPaneHTML` 间接驱动。

## 2. Java 用户如何使用
用户不直接实例化：`JEditorPane ep = new JEditorPane(); ep.setContentType("text/html"); ep.setText(html);` 后 `ep.getAccessibleContext()` 返回 `AccessibleJEditorPaneHTML`（内容类型切换触发），其子节点即 AccessibleHTML 构建的元素访问树（段落/图标/表格各一个节点，根角色为 text）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`AccessibleHTML(JEditorPane)` 构造、`getAccessibleContext` 返回 RootHTMLAccessibleContext、TextElementInfo 提供 role=text。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JEditorPane.java`：`getAccessibleContext()` 在 HTML 内容下安装 `AccessibleJEditorPaneHTML`。

## 4. 测试用例设计（黑盒，`AccessibleHTMLTest`，22 断言）
1. shape：类包私有（mods==0）、implements Accessible、嵌套类 HTMLAccessibleContext/IconElementInfo/TableElementInfo/TextElementInfo 存在。
2. EDT 公开路径：`ep.getAccessibleContext()` 类名 `JEditorPane$AccessibleJEditorPaneHTML`、role==TEXT、双 p + a 文档 children==5、accessibleName null、六状态（enabled/focusable/visible/opaque/editable/multi-line）、getAccessibleText() 非 null。
3. 反射路径（tag 带 `--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED`）：`ctor.setAccessible(true)` 构造 AccessibleHTML(pane)，`getAccessibleContext()` 返回 `AccessibleHTML$RootHTMLAccessibleContext`、role==TEXT、children==5。
4. children 计数规律：1 个 p → 3、2 个 p → 5、p + a → 5（段落计数依赖内容结构）。
