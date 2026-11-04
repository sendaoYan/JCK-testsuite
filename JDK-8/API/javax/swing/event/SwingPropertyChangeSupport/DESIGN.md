# SwingPropertyChangeSupport 行为测试设计

## 1. API 功能
`javax.swing.event.SwingPropertyChangeSupport` 是 `java.beans.PropertyChangeSupport` 的 Swing 特化（final class），增加「是否在 EDT 上通知」的开关。

- **构造器**：`SwingPropertyChangeSupport(Object sourceBean)`（等价于 notifyOnEDT=false）与 `SwingPropertyChangeSupport(Object sourceBean, boolean notifyOnEDT)`。
- **本类方法**：`isNotifyOnEDT()`、`firePropertyChange(PropertyChangeEvent)`（notifyOnEDT=true 且非 EDT 时用 `SwingUtilities.invokeLater` 异步派发，否则同步派发）。
- **继承方法**：`addPropertyChangeListener(...)`、`removePropertyChangeListener(...)`、`firePropertyChange(String,Object,Object)` 及 int/boolean 重载、`getPropertyChangeListeners(...)`。

## 2. Java 用户如何使用
Swing 组件（如 `UIManager`）用它在 EDT 上安全派发属性变化：

```java
SwingPropertyChangeSupport support = new SwingPropertyChangeSupport(bean, true);
support.addPropertyChangeListener(evt -> {
    String name = evt.getPropertyName();
    Object old = evt.getOldValue(), nw = evt.getNewValue();
});
support.firePropertyChange("lookAndFeel", oldLaf, newLaf);   // 同步（若在 EDT）
support.firePropertyChange(new PropertyChangeEvent(bean, "x", 0, 1));
boolean onEDT = support.isNotifyOnEDT();
```

每个方法典型调用：构造（选择是否 EDT 通知）→ `addPropertyChangeListener` 注册 → `firePropertyChange(name,old,new)` 同步派发 → `getPropertyChangeListeners()` 回读 → `removePropertyChangeListener` 注销 → `isNotifyOnEDT()` 读取开关。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/UIManager.java:219`：
   ```java
   changeSupport = new SwingPropertyChangeSupport(...);
   ```
   `UIManager` 用它通知外观切换。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/AbstractAction.java` 同样用 SwingPropertyChangeSupport 派发属性事件。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 PropertyChangeSupport、public、非 abstract），新增 `final` 修饰符断言。
- 黑盒：用 notifyOnEDT=false 实例做**同步**派发验证——`firePropertyChange(name,old,new)` 与 int 重载、`firePropertyChange(PropertyChangeEvent)` 均被 listener 捕获且 propertyName/old/new/source 正确；`getPropertyChangeListeners()` 回读；移除后不再通知；两参构造器 `isNotifyOnEDT()` 返回 true。
- 自动化：`testWhiteBox()` + `testBlackBox()`；EDT 异步分支不触发（只测同步路径），headless-safe。
