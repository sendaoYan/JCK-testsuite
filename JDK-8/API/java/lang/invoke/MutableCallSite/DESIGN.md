# java.lang.invoke.MutableCallSite — 设计方案

## 1. API 功能
可变目标调用点：两个 public 构造 `(MethodHandle)`（null → NPE）与 `(MethodType)`（null → NPE；初始 target 是同型占位符，调用抛 IllegalStateException "uninitialized call site"）。`setTarget` 换目标后通过 dynamicInvoker 的调用立即走新分支（同步语义）；类型不匹配 → WrongMethodTypeException（消息 "MethodHandle(X)Y should be of type (P)R"）。`static syncAll(MutableCallSite[])` 强制让其它线程看到更新（空数组合法、null 数组/null 元素 → NPE）。

## 2. Java 用户如何使用
```java
MutableCallSite site = new MutableCallSite(trueBranch);
site.setTarget(falseBranch);
Object r = site.dynamicInvoker().invoke(); // 立即走 falseBranch
MutableCallSite.syncAll(new MutableCallSite[]{ site });
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/MutableCallSite.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/MutableCallSite.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/MutableCallSite.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/MutableCallSite.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/MutableCallSite
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | setTarget→invokeExact 交替翻转（1,2,1,2），缓存的旧 dynamicInvoker 句柄同样跟随 | testBranchSwitching |
| P0 | getTarget/setTarget 恒等往返（setTarget(h) 后 getTarget()==h） | testGetTargetRoundTrip |
| P0 | setTarget(null) NPE；错型 WMTE "should be of type"；两个构造 null 参数 NPE | testNegativeMatrix |
| P1 | 占位 target：类型精确、稳定（两次 getTarget 相同）、invoke/dynamicInvoker 抛 ISE "uninitialized call site"、setTarget 后可用 | testPlaceholderSemantics |
| P1 | syncAll：空数组 no-op、真数组后站点仍可调、null 数组与 null 元素均 NPE | testSyncAll |
| P2 | 白盒：2 个 public 构造、getTarget/dynamicInvoker final、setTarget 可覆写、syncAll static public | testWhiteBoxStructure |

易错点：
- dynamicInvoker() 每次 new 句柄（与 ConstantCallSite 相反），不要断言 ==。
- 27 侧 NPE 带 helpful 消息（"newTarget" is null 等），8 侧消息为 null。
