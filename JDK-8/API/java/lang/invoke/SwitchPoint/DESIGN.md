# java.lang.invoke.SwitchPoint — 设计方案

## 1. API 功能
一次性全局开关：`guardWithTest(target, fallback)` 生成委托句柄——开关有效期间走 target，`invalidateAll(SwitchPoint[])` 之后（永久、立即、幂等）所有已生成的和新生成的 guard 都走 fallback。`hasBeenInvalidated()` 反映状态。javadoc 明示失效后 guardWithTest "may ignore T and return F"（实测直接返回 fallback 型句柄，跳过 target 的类型/空检查）。

## 2. Java 用户如何使用
```java
SwitchPoint sp = new SwitchPoint();
MethodHandle guard = sp.guardWithTest(newImpl, oldImpl);
Object r1 = guard.invoke();          // newImpl
SwitchPoint.invalidateAll(new SwitchPoint[]{ sp });
Object r2 = guard.invoke();          // oldImpl，同一句柄已翻转
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/SwitchPoint.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/SwitchPoint.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/SwitchPoint.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/SwitchPoint.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/SwitchPoint
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 失效前两个 guard 均走 t；invalidateAll 后两个旧 guard 与新 guard 均走 f，翻转稳定 | testMultiGuardFlip |
| P0 | 有效开关上 guardWithTest 类型不匹配 → IAE "target and fallback types must match: ()X != ()Y"；null target/fallback → NPE；invalidateAll null 数组/null 元素 → NPE | testNegatives |
| P1 | invalidateAll 幂等（二次调用无变化）、跨开关独立、空数组 no-op | testIdempotenceAndIndependence |
| P1 | 已失效开关 guardWithTest 可无视 target：返回 fallback 型句柄并算 fallback 值 | testInvalidatedIgnoresTarget |
| P1 | 参数原样透传到两个分支（permuteArguments 反转分支验证调用序） | testGuardArgumentPassThrough |
| P2 | 白盒：单 public 构造、非 final（可子类化）、invalidateAll static public | testWhiteBoxStructure |

易错点：
- 类型不匹配负例必须在未失效开关上测——失效后 guardWithTest 直接返回 fallback，不再检查 target（javadoc 允许）。
- guard 每次生成新句柄（g1 != g2）。
