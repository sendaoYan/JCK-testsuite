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

## 契约深化（2026-11）

### 1. API 功能（新增维度）
恰 9 个公共方法（getClass/hashCode/equals/toString/notify/notifyAll/wait×3）；clone/finalize
protected 不入公共面；equals/hashCode/toString/clone 可覆写；finalize 在 27 侧
**@Deprecated(since=9, forRemoval=true)**（测试 MODERN 分支 + 反射读 since/forRemoval，JDK-8
无注解且无该二成员——反射法两侧可编译）；wait 参数校验 IAE **先于** IMSE（未持锁 + wait(-1)
得 "timeout value is negative"）；wait(0,0)==wait(0) 无限等待（测试禁用）。

### 2. 使用方式（新增）
Pair 值语义 equals 契约矩阵 + HashMap/HashSet 联动；identityHashCode(null)==0；未覆写
hashCode==identityHashCode + toString@hex 逐字一致；getClass() == 身份（数组长度无关、
Object.class.getClass()==Class.class、嵌套二进制名 $）；toString 数组前缀 [I@/[J@/
[Ljava.lang.String;@；notify 恰一唤醒 / notifyAll 全唤醒 / wait 释放监视器
（TIMED_WAITING+holdsLock）/ 中断等待（InterruptedException + 状态清空）；clone CNSE 消息==
二进制名；任意对象作监视器 + holdsLock 重入真值表。

### 3. 参考来源
Oracle javadoc Object.wait(long,int)（消息 "timeout value is negative" /
"nanosecond timeout value out of range"、nanos∈[0,999999]）；JDK-27 Object.java（finalize
注解）。SUID/异常消息均双侧实测一致。

### 4. 测试用例设计 + 易错规范细节（新增方法）
testWhiteBoxStructure；testFinalizeDeprecation；testEqualsContractValueSemantics；
testHashCodeContract；testGetClassIdentity；testToStringFormats；testMonitorErrorsExtended；
testWaitArgumentValidation；testNotifySelectsExactlyOne；testNotifyAllWakesAll；
testWaitReleasesLock；testInterruptDuringWait；testCloneDetails；testAnyObjectAsMonitor；
testEqualsOnWrappersAndArrays。

易错：数组的 getClass() 比较 先赋 Class<?>；wait(0,0) 会永等；String 当锁有常量池共享风险。
