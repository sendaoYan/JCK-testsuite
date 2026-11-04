# java.awt.Container 黑盒行为测试设计（JDK-8）

## 1. API 功能
Component 子类，容器：add 五形态（Component/String,comp/comp,int/comp,Object/comp,Object,int）、
remove(int/Component)/removeAll/getComponent/getComponentCount/getComponents、z-order get/setComponentZOrder、
布局 setLayout/doLayout/layout/validate/invalidate、命中 getComponentAt/findComponentAt/locate/isAncestorOf、
传播 setFont/applyComponentOrientation、容器事件（ADDED=300/REMOVED=301 同步分发）、焦点遍历策略。

## 2. Java 用户如何使用
`Container c = new Container(); c.setLayout(new FlowLayout()); c.add(b1); c.add(b2);`
`c.doLayout()` 后读子组件精确 bounds；`c.remove(0)/c.removeAll()`；嵌套容器对比 getComponentAt（直接子）与
findComponentAt（最深叶）。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Container.java`
（addImpl 维护 component 列表并 re-parent；layout 委派 layoutMgr；validate 条件 validateTree；processContainerEvent）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集）→ `testStructure`。
- add/Remove/顺序/FlowLayout 精确 bounds/invalidate-validate 翻转/焦点策略往返 → `testBlackBox`。
- add 重载族返回值、index==count/-1 追加、constraints 形态 → `testAddOverloadsAndExceptions`。
- getComponents 拷贝语义 + 重 add re-parent + isAncestorOf 传递性 + z-order → `testComponentsCopyAndReparenting`。
- 容器事件同步分发精确 id/source/child/container、remove 停发 → `testContainerEvents`。
- setFont 传播/null 清除、applyComponentOrientation 递归、嵌套命中深浅对比、
  **普通 Container 默认布局是 null 而非 FlowLayout（易错）**、insets 全 0 → `testPropagationAndHitTests`。
- 反例与精确异常：`add(null)` 三形态→NullPointerException；`add(comp,99)/(-2)`→IllegalArgumentException；
  `remove/getComponent` 越界→ArrayIndexOutOfBoundsException；`remove(非子)`→no-op；
  `applyComponentOrientation(null)`→NPE。
- JDK-8 侧 149 断言通过（DISPLAY=:77，@key headful）。
