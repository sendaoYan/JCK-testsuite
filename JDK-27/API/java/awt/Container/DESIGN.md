# java.awt.Container 黑盒行为测试设计

## ① API 功能分组
- 子组件管理：`add(Component)`、`add(String,Component)`、`add(Component,int)`、`add(Component,Object)`、`add(Component,Object,int)`、`remove(int/Component)`、`removeAll`、`getComponent`、`getComponentCount/countComponents`、`getComponents`。
- z-order：`getComponentZOrder/setComponentZOrder`。
- 布局：`setLayout/getLayout/doLayout/layout/validate/invalidate/getInsets/insets`。
- 命中/祖先：`getComponentAt/locate/findComponentAt/isAncestorOf`。
- 传播：`setFont` 向子传播、`applyComponentOrientation` 递归。
- 事件：`addContainerListener/removeContainerListener/getContainerListeners`（COMPONENT_ADDED=300/REMOVED=301 同步分发）。
- 焦点遍历：`setFocusTraversalPolicy/getFocusTraversalPolicy`。

## ② Java 用户用法
```java
Container c = new Container();
c.setLayout(new FlowLayout());
c.add(new Button("a")); c.add(new Button("b"));
c.validate();
c.getComponentCount(); c.getComponent(0); c.remove(0);
```
组合链路：设布局 → add 组件 → doLayout → 读回子组件 bounds/顺序 → remove/removeAll；嵌套容器做 getComponentAt/findComponentAt 深浅对比。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Container.addImpl` 维护 `component` 列表并触发 addNotify，重复 add 触发 re-parent（`Container.java`）。
- `Container.layout` 调用 `layoutMgr.layoutContainer(this)`；layout 为 null 时仅置 valid（`Container.java`）。
- `Container.validate` 仅当 `!isValid() && peer != null` 时 validateTree（`Container.java`）。
- `Container.processContainerEvent` 分发 ADDED/REMOVED（`Container.java`）。

## ④ 测试用例设计 + 自动化实现
- 三形态 `add` + `getComponentCount/getComponent/getComponents` 顺序 → `testBlackBox`。
- `remove(Component)/remove(int)/removeAll` → `testBlackBox`。
- FlowLayout 布局两组件精确 bounds + `layout()` 弃用别名 + `invalidate/validate/isValid` 翻转 → `testBlackBox`。
- `setFocusTraversalPolicy/getFocusTraversalPolicy` 往返（focus cycle root 下）→ `testBlackBox`。
- add 重载族返回值（String/int 形态返回组件本身）、index==count 追加、index=-1 追加、constraints 形态、index 0 插入 → `testAddOverloadsAndExceptions`。
- `getComponents` 拷贝语义（改数组不影响容器、每次新数组）；重 add 重定向父指针；`isAncestorOf` 传递/不反射/不逆向；z-order 移动 → `testComponentsCopyAndReparenting`。
- 容器事件同步分发、精确 id=300/301、source/child/container 精确、remove 监听器后停发 → `testContainerEvents`。
- `setFont` 传播与 null 清除、`applyComponentOrientation` 递归；嵌套命中 `getComponentAt`=直接子 vs `findComponentAt`=最深叶、未命中 null、`locate` 别名；普通容器 insets 全 0、默认布局 **null**（非 FlowLayout）→ `testPropagationAndHitTests`。

## ⑤ 反例与精确异常
- `add(null)`/`add(null,int)`/`add(String,null)` → NullPointerException（三种形态一致）。
- `add(comp,99)`/`add(comp,-2)` → IllegalArgumentException；失败后无部分状态。
- `remove(越界)`/`getComponent(越界)` → ArrayIndexOutOfBoundsException；空容器 `remove(0)` 同样 AIOOBE。
- `remove(非子组件)` → no-op（无异常）。
- `applyComponentOrientation(null)` → NPE。

自动化实现位于同目录 `ContainerTest.java`：`check/checkThrows` 计数断言，`main` 打印 `PASS (N assertions)`；@key headful（DISPLAY=:77），通过 116 断言。
