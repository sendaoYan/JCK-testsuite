# java.awt.Insets 黑盒行为测试设计

## 1. API 功能
- 构造：`Insets(int top, int left, int bottom, int right)`（无默认构造器）。
- 公开可变字段：`top`/`left`/`bottom`/`right`（int）。
- 修改：`set(int,int,int,int)`。
- 查询/值语义：`equals`、`hashCode`、`toString`。
- 复制：`clone()`。

## 2. Java 用户如何使用
组件/容器的边距描述符，布局与绘制常用：`Container.getInsets()` 得到边框占位，再据此缩进内容区；对话框/工具提示内边距计算。
```java
Insets insets = panel.getInsets();
int innerX = insets.left;
int innerW = width - insets.left - insets.right;
Insets copy = (Insets) insets.clone();
```
组合链路：构造 → `set` → `clone` → `equals` 比较 → `toString` 展示。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Insets.java`：`hashCode` 用组合整数公式；`clone` 走 `super.clone()`；`toString` 固定 `java.awt.Insets[top=..,left=..,bottom=..,right=..]`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/CardLayout/RemoveComponentTest/RemoveComponentTest.java`（`new Insets` 构造 + `set`）、`test/jdk/java/awt/TrayIcon/TrayIconTest.java`（`new Insets` 定义内边距）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 构造器赋值 top/left/bottom/right。
- `set` 更新四个字段。
- `equals`：相等/自反/不同 top/null/异类；`hashCode` 相等对象同值 + 确定性。
- `toString` 精确格式。
- `clone`：返回 `Insets`、独立对象、值相等、修改副本不影响原对象。
- 组合链：构造 → `set` → `clone` → 比较 → 修改副本 → 验证原对象不变。
断言 26 项，纯值对象精确断言（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：字段顺序；set() 重置；equals/hashCode=577；clone 独立；负值序列化往返；sUID；toString 精确。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
