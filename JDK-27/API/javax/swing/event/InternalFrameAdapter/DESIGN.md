# javax.swing.event.InternalFrameAdapter 测试设计

## 1. API 功能

`InternalFrameAdapter` 是 `InternalFrameListener` 的便捷适配器抽象类：继承 `java.lang.Object`、实现 `InternalFrameListener`，把 7 个接口方法全部实现为空操作（no-op），让用户只重写关心的回调。7 个方法：

- `internalFrameOpened(InternalFrameEvent e)`
- `internalFrameClosing(InternalFrameEvent e)`
- `internalFrameClosed(InternalFrameEvent e)`
- `internalFrameIconified(InternalFrameEvent e)`
- `internalFrameDeiconified(InternalFrameEvent e)`
- `internalFrameActivated(InternalFrameEvent e)`
- `internalFrameDeactivated(InternalFrameEvent e)`

构造器为 `protected InternalFrameAdapter()`（供子类调用）。事件 `InternalFrameEvent`（继承 `AWTEvent`）携带 `getInternalFrame()` 与 `getID()`（7 个 id 常量）。

## 2. Java 用户如何使用

构造：用户匿名子类化并只重写需要的方法：

```java
JInternalFrame f = new JInternalFrame("t");
InternalFrameAdapter a = new InternalFrameAdapter() {
    public void internalFrameOpened(InternalFrameEvent e) { /* 只关心打开 */ }
};
f.addInternalFrameListener(a);
```

典型组合链路：`new InternalFrameAdapter(){ 重写 internalFrameOpened } → f.addInternalFrameListener(a) → 显示 f → internalFrameOpened(new InternalFrameEvent(f, INTERNAL_FRAME_OPENED)) → 读 e.getInternalFrame()`。未重写的方法保持 no-op，调用不抛异常。

## 3. 开源代码实际用法调研

1. JDK 实现 `BasicInternalFrameUI.BorderListener` 子类化 `MouseInputAdapter`（不是本类）；而 `BasicInternalFrameUI` 内部另有 `InternalFrameListener` 直接实现（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicInternalFrameUI.java:1454`）：
   ```java
   protected class BasicInternalFrameListener implements InternalFrameListener { ... }
   ```
2. `JInternalFrame` 提供 `addInternalFrameListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JInternalFrame.java:1492`），`InternalFrameAdapter` 作为省写方式被用户与 `test/jdk/javax/swing/JInternalFrame/` 用例广泛使用。
3. `InternalFrameAdapter` 自身源码（`src/java.desktop/share/classes/javax/swing/event/InternalFrameAdapter.java`）即 7 个空方法 + `protected` 构造器，是「适配器空实现」范式。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`InternalFrameAdapterTest.testBlackBox()`）：
- 匿名子类重写 7 个方法并记录最近回调名，构造 `InternalFrameEvent`，逐一调用 7 个方法，断言各自被重写回调、`getInternalFrame()` round-trip。
- 再创建「不重写任何方法」的普通子类，调用 7 个 no-op 方法，验证不抛异常（空实现可调用）。
- 断言适配器实例 `instanceof InternalFrameListener`。

白盒保留原有结构断言，新增：7 方法为 void 且非抽象、构造器 `protected`。

## 5. 版本差异（JDK-8 vs JDK-27）
- javap 确认：JDK-8 上 `InternalFrameAdapter()` 构造器为 **public**；JDK 9+（含 JDK-27）收窄为 **protected**。JDK-8 侧测试钉住 public，JDK-27 侧测试钉住 protected。
