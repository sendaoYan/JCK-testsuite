# javax.swing.tree.ExpandVetoException 测试设计

## 1. API 功能
受检异常（extends Exception）：TreeWillExpandListener 否决展开/折叠时抛出；
两个构造器 `ExpandVetoException(TreeExpansionEvent)` 与 `(TreeExpansionEvent, String)`，
被否决事件存于 **protected 字段 event**（无 public 访问器）。

## 2. Java 用户如何使用
`tree.addTreeWillExpandListener(new TreeWillExpandListener() { public void treeWillExpand(TreeExpansionEvent e)
throws ExpandVetoException { throw new ExpandVetoException(e, "locked"); } });` 阻止节点展开。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/ExpandVetoException.java`；
JTree.basicExpandChild 处 catch；`test/jdk/javax/swing/JTree/` 展开相关测试。

## 4. 测试用例设计 + 易错细节
- 两构造器消息/事件接线：单参消息 null、双参消息保留、null 事件被接受不校验。
- event 字段经反射 getDeclaredField 读取并断言身份一致；无 public getEvent（遍历 getMethods 验证）。
- 受检语义：非 RuntimeException/Error、可被 catch(Exception) 捕获、binary name 精确。
- 恰好 2 个 public 构造器；toString/stackTrace 继承 Throwable 行为。
- 版本差异（javap/实测确认）：JDK-8 反射 setAccessible 可读 protected 字段值；
  JDK-9+ 强封装（java.desktop 未开放 unnamed 模块）抛 InaccessibleObjectException——
  JDK-27 侧测试保留 Field 元数据（断言 protected 修饰符）但跳过取值断言。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
