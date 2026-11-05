# LockInfo — 设计方案

## 1. API 功能
`java.lang.management.LockInfo`（类）描述线程所持有的锁：`getClassName()`、`getIdentityHashCode()`、`toString()`、静态 `from(CompositeData)`。构造 `LockInfo(String className, int identityHashCode)`。

## 2. Java 用户如何使用
```java
LockInfo lock = threadInfo.getLockInfo();
String cls = lock.getClassName();
int id = lock.getIdentityHashCode();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/ThreadMXBean` 断言锁的 className/identityHashCode。
- 监控工具（jconsole/JMC）用 LockInfo 展示线程持有的监视器。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Object + 构造器 + getter + from 返回类型。
黑盒：构造后 getClassName/getIdentityHashCode 往返；identity hash 0；toString 含 className。
对应 `LockInfoTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 构造 null className → NPE；className/identityHashCode 正负值（含 Integer.MIN/MAX）原样回读。
- toString 精确格式 `类名@十六进制身份哈希`（与 `Integer.toHexString` 一致）。
- equals/hashCode 为 identity 语义（未重写）：两个同 className 同 hash 的实例不等。
- `from(null)` → null。
- 真实场景身份：blocked 线程的 `ThreadInfo.getLockInfo().getClassName()` 等于持锁 monitor 的对象类名（锁链 fixture 实证）。
