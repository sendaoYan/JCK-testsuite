# java.lang.invoke.VolatileCallSite — 设计方案

## 1. API 功能
目标以 volatile 语义发布的可变调用点（与 MutableCallSite 是兄弟类，API 完全同构）：`(MethodHandle)`/`(MethodType)` 两构造、setTarget 的更新对读线程立即可见（本测试以单线程同步语义验证"写后读必见"）、getTarget/dynamicInvoker 为 final。无 syncAll（那是 MutableCallSite 的静态方法，volatile 语义本身保证可见性）。

## 2. Java 用户如何使用
```java
VolatileCallSite site = new VolatileCallSite(a);
site.setTarget(b);
Object r = site.dynamicInvoker().invoke(); // b，volatile 可见
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/VolatileCallSite.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/VolatileCallSite.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/VolatileCallSite.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/VolatileCallSite.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/VolatileCallSite
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 与 Mutable 全语义对齐：分支交替翻转、缓存 invoker 跟随、getTarget 恒等往返 | testBranchSwitching / testGetTargetRoundTrip |
| P0 | setTarget(null) NPE、错型 WMTE "should be of type"、两构造 null NPE | testNegativeMatrix |
| P1 | 占位 target ISE "uninitialized call site"、setTarget 后可用 | testPlaceholderSemantics |
| P1 | 两构造产出等价（type/dynamicInvoker/调用值一致） | testConstructorParity |
| P1 | 与 MutableCallSite 兄弟类（互非父子、共父 CallSite） | testTypeRelation |
| P2 | 白盒：2 public 构造、final getTarget/dynamicInvoker、可覆写 setTarget | testWhiteBoxStructure |

易错点：
- 不开线程验证 volatile（规则禁止），以"写后立即读必见"表达同步契约。
- syncAll 不接受 VolatileCallSite[]（类型层面即拒绝）。
