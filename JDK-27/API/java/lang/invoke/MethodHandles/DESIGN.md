# java.lang.invoke.MethodHandles — 设计方案

## 1. API 功能
方法句柄工厂与组合器集合（final 工具类，全部静态方法）：`lookup/publicLookup`、`identity/constant/zero/empty`、数组构造器/元素访问、`insertArguments/dropArguments/permuteArguments/filterArguments/filterReturnValue/foldArguments`、`guardWithTest/catchException/tryFinally`、`throwException/explicitCastArguments/arrayConstructor/arrayElementGetter`、`reflectAs`。

## 2. Java 用户如何使用
```java
MethodHandles.Lookup lk = MethodHandles.lookup();
MethodHandle add = lk.findStatic(Math.class, "addExact", methodType(int.class, int.class, int.class));
int r = (int) add.invokeExact(2, 3); // 5
MethodHandle dbl = MethodHandles.insertArguments(add, 1, 21);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/invoke/MethodHandles.java`（实现本体）。
- JDK `test/jdk/java/lang/invoke/`（698 行为测试：FiltersTest/GuardTest/CatchExceptionTest/TryFinallyTest 等）。
- Spring/Gradle 运行时与 Mockito/ByteBuddy 用 filterArguments/foldArguments 做调用拦截与 AOP 织入。

## 4. 测试用例设计 + 自动化实现
白盒：final/public、无私有构造、组合器方法全存在、reflectAs 签名。
黑盒：identity/constant/zero/empty 语义；insertArguments/dropArguments/permuteArguments/filterArguments/filterReturnValue 精确结果；foldArguments(add3,dbl)→(int,int)int==9；guardWithTest 真假两分支；catchException(thrower,ISE,常量-99) 捕获后返回；throwException 必抛；explicitCastArguments 宽化；tryFinally cleanup 收尾值；arrayConstructor/arrayElementGetter 组合。
对应 `MethodHandlesTest.java` 的 `testBlackBox()`。