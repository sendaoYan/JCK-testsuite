# java.lang.invoke.CallSite — 设计方案

## 1. API 功能
invokedynamic 调用点抽象（abstract 类）：`type/target/dynamicInvoker`、`setTarget/makePreinitialized`、`GET_TARGET/SET_TARGET` 内部方法句柄常量。

## 2. Java 用户如何使用
```java
CallSite cs = new MutableCallSite(MethodType.methodType(int.class));
cs.setTarget(MethodHandles.constant(int.class, 42));
int v = (int) cs.dynamicInvoker().invoke(); // 42
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/CallSiteTest.java`。
- StringConcatFactory/LambdaMetafactory 链接产物即 ConstantCallSite；javadoc 强调 dynamicInvoker 与 target 一致性。

## 4. 测试用例设计 + 自动化实现
白盒：abstract、protected 构造、type/target/dynamicInvoker/setTarget 方法存在。
黑盒：MutableCallSite 基础上验证 type() 一致；target 换后 dynamicInvoker 立即生效；setTarget(Mutable) 与三种子类共同行为；setTarget(null)→NPE、错类型→ClassCastException；makePreinitialized 门控 false 默认。
对应 `CallSiteTest.java` 的 `testBlackBox()`。