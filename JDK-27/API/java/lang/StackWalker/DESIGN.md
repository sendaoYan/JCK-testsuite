# StackWalker — 设计方案

## 1. API 功能
`java.lang.StackWalker`（final）遍历当前线程栈帧：
- 工厂：`getInstance()`、`getInstance(Option)`、`getInstance(Set<Option>)`、`getInstance(Set, estimate)`（estimate 必须 >0，否则抛 IllegalArgumentException）
- 遍历：`walk(Function<Stream<StackFrame>,T>)`、`forEach(Consumer)`
- 调用者：`getCallerClass()`
- 选项：`StackWalker.Option`（`RETAIN_CLASS_REFERENCE`、`DROP_METHOD_INFO`、`SHOW_REFLECT_FRAMES`、`SHOW_HIDDEN_FRAMES`）

## 2. Java 用户如何使用
- 代替 `new Throwable().getStackTrace()` 获取高效、可控的栈信息。
```java
String caller = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getName();
List<StackFrame> frames = StackWalker.getInstance().walk(s -> s.limit(5).toList());
```

## 3. 开源代码实际用法调研
- Log4j2/`System.Logger` 实现用 `StackWalker` 定位日志调用点。
- JDK `test/jdk/java/lang/StackWalker` 系列验证帧遍历、选项组合、`getCallerClass`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.StackWalker")` 验证 final、非接口。
黑盒：`walk` 至少一帧且能看到 `main`；`forEach` 不抛；`getCallerClass` 返回本类；四种 `getInstance` 变体非空。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- walk 第一帧精确闭环：`walk(s->s.findFirst())` 的第一帧是**调用 walk 的方法本身**（className/methodName/getFileName(".java")/getLineNumber>0/getByteCodeIndex>=0/isNativeMethod==false 全断）。
- RETAIN_CLASS_REFERENCE 面：`getDeclaringClass()==测试类`、`getDescriptor()=="()V"`（静态无参 void）、`getMethodType().toString()=="()void"`、`toMethodDescriptorString()=="()V"`；**无该 option 时 getDeclaringClass/getCallerClass/getMethodType/getDescriptor 全部抛 UnsupportedOperationException**（消息实测锁定：`No access to RETAIN_CLASS_REFERENCE` / `This stack walker does not have RETAIN_CLASS_REFERENCE access`——注意是 UOE 不是 IllegalStateException，javadoc 与实测一致）。
- DROP_METHOD_INFO（JDK-24+ 新 option）：className/getDeclaringClass 可用，getMethodName/getFileName/getLineNumber/getByteCodeIndex/isNativeMethod/toStackTraceElement/getMethodType/getDescriptor 全抛 UOE。
- Option 枚举：JDK-27 实测 **4 值**（RETAIN_CLASS_REFERENCE/DROP_METHOD_INFO/SHOW_REFLECT_FRAMES/SHOW_HIDDEN_FRAMES，声明序锁定）；valueOf 未知 → IAE、null → NPE、values() 新数组。
- getInstance 全变体：()`/(Option)×4/(Set)`/(Set,estimate)`；estimate 0 与 -1 → IAE；`getInstance(Set.of())` 与 `getInstance()` 行为等价。
- SHOW_REFLECT_FRAMES：Method.invoke 反射调用目标方法内断言 main 之下窗口内可见 `java.lang.reflect.Method.invoke` 帧、plain walker 同窗口不可见（窗口=第一帧到 main 帧，规避 harness 反射调用 main 的干扰）。
- 跨线程：命名 Runnable 子线程内 walk 得到**子线程自己的栈**（首帧 `StackWalkerTest$Child.run`、底 `java.lang.Thread.run`、无父线程测试类帧）；CountDownLatch 有界等待。
- walk 生命周期：`walk(s->null)==null`、walk 返回后使用流 → ISE `This stack stream is not valid for walking.`（消息精确）、walk(null)/forEach(null) → NPE、`skip(1)` 帧与 `Throwable.getStackTrace()[1]` 同物理帧（methodName 相等）。
- getCallerClass 深度：helper 一层/两层/嵌套类调用各返回对应类；toStackTraceElement 与 Throwable.getStackTrace 同帧一致（className/methodName/fileName/line）。

### 使用方式
- 同前；跨线程用 latch+join 有界等待，不留非 daemon 线程。

### 参考来源
- Oracle Java SE 27 javadoc（StackWalker/Option/StackFrame 各方法 Throws 契约）。
- 本仓库 JDK 27.0.1 实测探针（/tmp/lcI-exp/ProbeSW27b/ProbeDrop）。

### 测试用例设计 + 易错规范细节
- 新增方法：testWalkFirstFrameExact、testForEachRealStack、testNoRetainClassAccess、testOptionsEnum、testGetInstanceVariants、testDropMethodInfo、testWalkDeepNesting、testNestedClassFrame、testCrossThreadStack、testShowReflectFrames（reflectTarget+窗口扫描）、testShowHiddenFrames、testWalkLifecycle、testGetCallerClassDeep、testToStackTraceElementConsistency。
- 易错点：brief 预告的「getDeclaringClass 无 option 抛 IllegalStateException」实测为 **UnsupportedOperationException**（以实测为准锁定）；MethodType.toString 对 void 返回类型打印 `()void` 而 descriptor 是 `()V`；断言 main 之下反射帧时必须限制窗口（jtreg harness 用反射调 main，其上有 Method.invoke 帧）；lambda 内 walk 第一帧是**外层方法**不是 lambda。
