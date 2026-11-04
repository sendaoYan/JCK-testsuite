# java.lang.invoke.LambdaMetafactory（JDK-8 侧）— 设计方案

## 1. API 功能
`invokedynamic` 指令的引导方法工厂（public 工具类，JDK-8 上非 final、含 public 无参构造）：
- `metafactory(lookup, name, invokedType, samMethodType, implMethod, instantiatedMethodType)`：把一个可直接访问的 MethodHandle 实现链接成函数式接口实例，返回 `ConstantCallSite`；`invokedType` 的参数前缀即「捕获参数」。
- `altMetafactory(lookup, name, invokedType, Object[])`：变参尾部携带 `samMethodType, implMethod, instantiatedMethodType, FLAG_SERIALIZABLE, FLAG_MARKERS, count, marker接口..., FLAG_BRIDGES, count, bridge MethodType...`。
- 标志常量：`FLAG_SERIALIZABLE=1`、`FLAG_MARKERS=2`、`FLAG_BRIDGES=4`。

## 2. Java 用户如何使用
```java
CallSite cs = LambdaMetafactory.metafactory(lookup, "len",
        MethodType.methodType(Fn.class),                       // ()Fn
        MethodType.methodType(Object.class, String.class),     // SAM 摘要签名
        impl, MethodType.methodType(String.class, String.class)); // 实现/实例化
Fn f = (Fn) cs.dynamicInvoker().invokeExact();
```

## 3. 参考来源（只列路径）
- Oracle javadoc（Java 8 API 规范）：`java.lang.invoke.LambdaMetafactory`
- OpenJDK 源码：`src/java.base/share/classes/java/lang/invoke/LambdaMetafactory.java`
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/invoke/lambda/`（学等价类划分，代码自写）
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke`

## 4. 测试用例设计（`LambdaMetafactoryTest.java`）

| 优先级 | 测试点 | 方法 |
| --- | --- | --- |
| P0 | FLAG_SERIALIZABLE/MARKERS/BRIDGES 常量存在性与精确值 1/2/4；metafactory/altMetafactory 精确签名 + throws LambdaConversionException；JDK-8 特有：public 无参构造、非 final | testLambdaMetafactory / testStructureDeep |
| P0 | metafactory→CallSite→dynamicInvoker 强转→真调用 round-trip 精确值；CallSite.type()==()Fn；getTarget().type()；ConstantCallSite；两次链接独立 CallSite | testMetafactoryRoundTrip |
| P0 | 标准接口 Function<String,Integer>/Supplier/void+副作用计数接口；泛型 SAM 的装箱返回 | testStandardInterfaces |
| P1 | invokedType 捕获前缀：`(String)Fn2` 每次调用独立捕获，`apply(9)=="pre_9"` | testCapturedArgs |
| P1 | altMetafactory：FLAG_SERIALIZABLE→instanceof Serializable；FLAG_MARKERS→marker instanceof；组合标志；FLAG_BRIDGES→反射 `getMethod("len",Object.class)` 桥方法可用；非接口 marker→LCE "is not an interface" | testAltMetafactoryFlags |
| P0 | 负例（checkThrowsMsg 精确类+消息片段）：返回类型不兼容/参数元数/参数类型/void 实现对值 SAM/publicLookup（"Invalid caller"）/工厂类型非接口 → LambdaConversionException；非直接 handle（insertArguments 产物）→ IllegalArgumentException "not a direct method handle"（JDK-8 措辞） | testNegativeConversions |
| P2 | 观察行为锁定：SAM 名不存在、双抽象方法接口在链接期均不抛（调用期才失败） | testNegativeConversions |

### 易错规范细节
- JDK-8 上 `metafactory` 用 `publicLookup` 失败是 **LambdaConversionException "Invalid caller"**，不是 IllegalAccessException（findStatic 阶段才会是 IAEx）。
- 非直接 MethodHandle（`insertArguments` 适配产物）在 JDK-8 抛 **IllegalArgumentException "not a direct method handle"**（JDK-9+ 改为 LambdaConversionException，见 JDK-27 侧）。
- 消息检查顺序：先返回类型、后参数（void 实现对值 SAM 报 "Type mismatch for lambda expected return" 而非参数错）。
- 捕获参数是 `invokedType` 的参数前缀，剩余实现方法参数必须与 SAM 参数逐个可转换。
- JDK-8 与 27 措辞差异取公共子串断言（"is not an interface"）。
