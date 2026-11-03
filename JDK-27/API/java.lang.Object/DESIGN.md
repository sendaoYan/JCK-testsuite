# Object — 设计方案

## 1. API 功能
`java.lang.Object` 是所有类的超类，提供：
- 查询：`getClass()`（final）、`hashCode()`、`toString()`、`equals(Object)`
- 克隆：`clone()`（protected，需实现 `Cloneable`，否则抛 `CloneNotSupportedException`）
- 并发原语：`wait()/wait(long)/wait(long,int)`、`notify()`、`notifyAll()`（均为 final；未持有监视器抛 `IllegalMonitorStateException`）

## 2. Java 用户如何使用
- `equals/hashCode` 契约是 `HashMap`/`HashSet` 正确性的基础；子类重写 `equals` 必须同时重写 `hashCode`。
- `clone()`：实现 `Cloneable` 后调用 `super.clone()` 得到浅拷贝（引用字段共享）。
- `wait/notify`：在 `synchronized` 块内持有监视器后，生产者/消费者通过 `wait` 阻塞、`notify` 唤醒。
```java
synchronized (lock) { while (!ready) lock.wait(); }
synchronized (lock) { ready = true; lock.notify(); }
```

## 3. 开源代码实际用法调研
- `HashMap`/`HashSet` 大量依赖 `Object.hashCode()`/`equals()`（JDK 源码 `java.util.HashMap`）。
- `Object.clone()` 是 `Arrays.copyOf`/集合拷贝之外，用户自定义浅拷贝的经典入口（JDK `test/jdk/java/lang/Object` 系列）。
- `wait/notify` 是 `java.util.concurrent` 早期原语（`AbstractQueuedSynchronizer` 之前），现在多被 `Lock`/`Condition` 替代，但仍是阻塞队列实现的基础。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Object")` 验证无父类、非接口、非抽象；`getClass/notify/notifyAll/wait*` 为 final；`hashCode` 为 native。
黑盒：`equals` 自反/对称/`null` 不等/不同对象不等；`hashCode` 稳定且等于 `identityHashCode`；`toString` 格式；`clone` 浅拷贝语义（引用字段共享）+ 非 `Cloneable` 抛异常；`wait/notify` 用 `synchronized` 确定性唤醒；`wait(timeout)` 超时返回。
对应 `<Simple>Test.java` 黑盒部分。
