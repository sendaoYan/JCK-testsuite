# java.util.EventListener 测试设计案（JDK-8）

> 覆盖目标：完整契约。EventListener 是零方法标记接口；契约面在结构（无方法/无字段）
> 与子类型关系（普通类实现、子接口扩展、EventListenerProxy 包装）上。

## 1. API 功能

- 标记接口，声明于 `java.util`，**无任何方法与字段**（反射锁定）。
- 用途：所有事件监听器接口的共同祖先（AWT/Swing/beans 等域监听器均继承它）。
- 关联抽象类 `EventListenerProxy<T extends EventListener>`：实现 EventListener 自身，
  提供 `getListener()` 取被包装的监听器（附加属性代理模式的基类）。

## 2. 使用方式

- `class MyListener implements EventListener {}`（直接实现）。
- `interface SubListener extends EventListener {}`（子接口扩展）。
- `class MyProxy extends EventListenerProxy<MyListener> { MyProxy(l) { super(l); } }`
  （包装代理，`getListener()` 取回原监听器）。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/EventListener.html`、`java/util/EventListenerProxy.html`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/EventListener*.java`：
  接口体为空；EventListenerProxy 为抽象类、泛型上界 `T extends EventListener`、
  构造仅存储引用（无 null 检查）、getListener 返回存储引用。

## 4. 测试用例设计（映射到 EventListenerTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 是接口、声明无方法 | testEventListener |
| P0 | 普通类可实现（instanceof 成立） | testEventListener |
| P1 | 声明于 java.util、无字段 | testStructure |
| P1 | 子接口扩展后实例仍是 EventListener；指派方向矩阵（MyListener 可指派、String 不可） | testStructure |
| P1 | EventListenerProxy：抽象类、自身是 EventListener | testStructure |
| P1 | getListener() 返回构造参数同一实例（==）、调用稳定 | testProxyContract |
| P2 | 代理不触碰被包装者状态；代理 equals 为同一性（包同一 listener 的两个代理不相等） | testProxyContract |

## 关键规范细节（易错）

- `getDeclaredMethods().length == 0` 且 `getDeclaredFields().length == 0`：若未来加入默认方法，此断言需同步更新。
- EventListenerProxy 是**抽象**类，不能直接实例化；测试用最小子类探针。
- 代理没有 equals 覆写：`new MyProxy(l).equals(new MyProxy(l)) == false`（同一性相等）。

## 注意事项

- 纯结构断言 + 最小探针，无全局副作用；headless-safe 不加 `@key`。
- JDK-8 与 JDK-27 的该 API 面完全一致（javap 逐项相同）。
