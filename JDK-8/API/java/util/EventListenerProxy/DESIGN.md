# java.util.EventListenerProxy 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。抽象类，经具体子类实例化。

## 1. API 功能

- **构造器**：`EventListenerProxy(T listener)`。
- **访问**：`getListener()`。
- **接口**：`implements EventListener`。

## 2. 使用方式

子类 `extends EventListenerProxy<MyListener>` 调用 `super(listener)`；`getListener()` 取回监听器。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/EventListenerProxy`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/EventListenerProxy.java`：
  构造器直接 `this.listener = listener`（不做 null 校验）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getListener 返回同一实例 + implements EventListener | testEventListenerProxy |
| P0 | null listener 原样存取（不 NPE） | testNullListener |
| P0 | 契约深化：equals/hashCode 遵循监听器/可作 EventListener | testProxyContracts |

## 关键规范细节

- 构造器不做 null 校验，`null` 可存取。
- `getListener()` 返回构造时传入的同一实例。

## 注意事项

纯内存；headless-safe 不加 `@key`。
