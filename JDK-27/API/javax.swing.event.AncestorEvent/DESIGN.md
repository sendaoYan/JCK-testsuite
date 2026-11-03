# AncestorEvent 行为测试设计

## 1. API 功能
`javax.swing.event.AncestorEvent` 是祖先组件变化事件，继承 `java.awt.AWTEvent`，用于通知子组件其祖先组件被添加/移除/移动。

- **常量**：`ANCESTOR_ADDED = 1`、`ANCESTOR_REMOVED = 2`、`ANCESTOR_MOVED = 3`。
- **构造器**：`AncestorEvent(JComponent source, int id, Container ancestor, Container ancestorParent)`。
- **查询**：`getAncestor()`（触发事件的祖先容器）、`getAncestorParent()`（祖先的父容器）、`getComponent()`（受影响的 JComponent，等价于 `(JComponent)getSource()`）、继承的 `getSource()`/`getID()`。

## 2. Java 用户如何使用
典型链路：组件被加入容器层级时，Swing 内部用 `AncestorNotifier` 派发事件，用户注册 `AncestorListener` 后在回调里读取事件：

```java
component.addAncestorListener(e -> {
    if (e.getID() == AncestorEvent.ANCESTOR_ADDED) {
        Container a = e.getAncestor();          // 新加入的祖先
        Container p = e.getAncestorParent();    // 祖先的父容器
        JComponent c = e.getComponent();        // 事件源组件
    }
});
```

每个方法的典型调用：`getSource()` 取得源组件、`getID()` 区分事件类型、`getAncestor()`/`getAncestorParent()` 读取层级关系、`getComponent()` 直接取得源组件（省去向下转型）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/AncestorNotifier.java:85/107/128`：
   ```java
   new AncestorEvent(source, id, ancestor, ancestorParent);
   ```
   内部在 `ANCESTOR_ADDED`/`REMOVED`/`MOVED` 三种情形下构造事件后调用 `fireAncestorAdded/fireAncestorRemoved/fireAncestorMoved`。
2. `test/jdk/javax/swing/AncestorNotifier/4817630/bug4817630.java`：回归用例验证祖先通知在组件树变化时的正确性，是 AncestorEvent 语义的直接消费者。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 AWTEvent、非接口/注解/枚举、public、非 abstract、无接口），新增三个常量字段存在性检查。
- 黑盒：断言三个常量值；构造 `ANCESTOR_ADDED`/`REMOVED`/`MOVED` 三种事件，验证 `getSource/getComponent/getAncestor/getAncestorParent/getID` 的 round-trip；覆盖 `ANCESTOR_REMOVED` 时 `ancestorParent == null` 的边界。
- 自动化：`testWhiteBox()` + `testBlackBox()`，多步组合（构造 → 各 getter 相互印证），全部 headless-safe。
