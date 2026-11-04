# java.lang.ref.PhantomReference — 设计方案

## 1. API 功能
虚引用：最弱的引用，`get()` 恒为 null（规范规定），仅用于对象“死后”的排队通知：
- 构造：`PhantomReference(T, ReferenceQueue<? super T>)`（唯一 public 构造器；queue 传 null 表示不入队）
- 查询：覆写 `get()`（恒 null）、覆写 `refersTo(T)`（仍可识别 referent）、继承 `isEnqueued()`
- 操作：`clear()`、`enqueue()`

## 2. Java 用户如何使用
替代 finalize 的延迟清理（Cleaner 即基于它）：
```java
ReferenceQueue<Object> q = new ReferenceQueue<>();
PhantomReference<Object> pr = new PhantomReference<>(resource, q);
assert pr.get() == null;          // 永远拿不到 referent
Reference<?> r = q.remove();      // referent 被回收后 pr 出现在队列
((PhantomReference<?>) r).clear();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/ref/PhantomReference.java`：javadoc 明确“get 恒 null 以免 resurrect 对象”；JDK 9 后推荐用 `Cleaner`。
- JDK `src/java.base/share/classes/jdk/internal/ref/CleanerImpl.java`：`PhantomCleanableRef extends PhantomCleanable`，内部即 PhantomReference+ReferenceQueue。
- JDK `test/jdk/java/lang/ref/PhantomReferentClearing.java`：验证 phantom referent 在入队前已被清除。

## 4. 测试用例设计 + 自动化实现
白盒：具体类、唯一 public 构造器 `(T, ReferenceQueue)`、覆写 `get()`/`refersTo()`。
黑盒（强可达 + 显式 enqueue，不依赖 GC）：
- referent 强可达时 `get()` 仍恒 null；
- `refersTo` 精确识别 referent（排除他者/null）；
- 手动 enqueue→isEnqueued→poll 同一引用→队列清空；
- clear 后 `refersTo(o)` false；
- null 队列构造合法且 enqueue 恒 false；null referent 合法；以 `Reference` 视角可用。
对应 `PhantomReferenceTest.java` 的 `testBlackBox()`。
