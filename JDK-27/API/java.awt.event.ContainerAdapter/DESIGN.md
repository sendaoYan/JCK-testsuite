# java.awt.event.ContainerAdapter 行为测试设计

## 1. API 功能
- **类型**：抽象适配器类
- **继承/实现**：实现 java.awt.event.ContainerListener
- **方法清单**：`componentAdded(ContainerEvent)`、`componentRemoved(ContainerEvent)`
- **语义**：ContainerAdapter 是 ContainerListener 的空实现适配器，把所有方法实现为 no-op，供用户只覆写感兴趣的回调。构造器受保护（不能直接实例化，需子类化）。

## 2. Java 用户如何使用
典型用法：`class MyAdapter extends ContainerAdapter { @Override public void componentAdded(...) { ... } }`，然后用 `Component.addXxxListener(new MyAdapter())` 注册；未覆写的方法安全空转。
组合链路：实现回调 → 注册到组件/工具 → 事件源触发 → 分派线程调用回调 → 回调内读取事件字段（getID/getSource 等）→ 注销。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/ContainerAdapter.java`（方法签名与 no-op 实现）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Component.java`（addXxxListener/removeXxxListener 与 enableEvents 注册路径）
- `~/git/jdk25u-dev/test/jdk/java/awt/event/`（真实事件分发用例，如 MouseEvent/KeyEvent/FocusEvent 测试以匿名类或 lambda 实现监听器并断言回调）

## 4. 测试用例设计 + 自动化实现
- **白盒**：反射断言类型形态（public/abstract/interface、继承 EventListener、方法数量与签名、public abstract 或 public no-op）。
- **黑盒**：实例化实现/子类，逐一调用每个回调（事件参数传 null，断言不抛异常且正确分派到对应方法）；单方法接口另测 SAM lambda；适配器断言只有被覆写的方法被分派。
- 对应 `ContainerAdapterTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
