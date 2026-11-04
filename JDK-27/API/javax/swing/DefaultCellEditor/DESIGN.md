# javax.swing.DefaultCellEditor 测试设计

## 1. API 功能
JTable/JTree 通用单元格编辑器：三种组件构造器、clickCountToStart、
getTableCellEditorComponent/getTreeCellEditorComponent、编辑生命周期事件。

## 2. Java 用户如何使用
`table.setDefaultEditor(Object.class, new DefaultCellEditor(combo))`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultCellEditor.java`；
AbstractCellEditor.java；JTable/JTree 编辑路径；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- **clickCountToStart 默认：文本 2、复选框 1、组合框 1**（易记错：组合框是 1 不是 2）。
- getCellEditorValue 跟随：文本=field.getText()、组合框=combo.getSelectedItem、复选框=Boolean。
- 表格/树编辑组件就是构造器组件本身，且值写入 field 文本。
- isCellEditable：null/非鼠标事件 true；MouseEvent 按 clickCount>=clickCountToStart 判定，
  setClickCountToStart(1) 后单击即可。
- stopCellEditing true + editingStopped；cancelCellEditing + editingCanceled；移除监听器后静默。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。

