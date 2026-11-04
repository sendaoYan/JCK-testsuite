# java.lang.invoke.SwitchPoint — 设计方案

## 1. API 功能
一次性开关（final 类）：`guardWithTest(target, fallback)` 生成在失效前选 target、失效后永久选 fallback 的守卫句柄；`invalidateAll` 批量翻转；`hasBeenInvalidated`。

## 2. Java 用户如何使用
```java
SwitchPoint sp = new SwitchPoint();
MethodHandle g = sp.guardWithTest(fastPath, slowPath);
SwitchPoint.invalidateAll(new SwitchPoint[]{ sp }); // 之后 g 走 slowPath
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/SwitchPointTest.java`。
- invokedynamic 去优化（deopt）模式：快速路径假设失效后统一翻转到解释路径， invokedynamic 引导实现常用于版本切换。

## 4. 测试用例设计 + 自动化实现
白盒：final、无私有构造、guardWithTest/invalidateAll/hasBeenInvalidated 存在。
黑盒：失效前 guard 走 target、invalidateAll 后既有 guard 永久走 fallback；两个独立 SwitchPoint 互不影响；guardWithTest 类型=()String 正确；types 不匹配的两路句柄仍按 target 类型统一。
对应 `SwitchPointTest.java` 的 `testBlackBox()`。
## 深化批次（2026-11-05）

新增：有效开关上 guardWithTest 类型不匹配 IAE "target and fallback types must match"、null target/fallback NPE、invalidateAll null 矩阵、两个预生成 guard 同时翻转、失效后可无视 target（返回 fallback 型句柄）、参数透传两分支。（17→33 断言）
