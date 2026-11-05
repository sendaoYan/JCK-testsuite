# ClassFileTransformer — 设计方案

## 1. API 功能
`java.lang.instrument.ClassFileTransformer`（接口）在类加载/重定义/重转换时变换字节码。两个 default `transform`：5 参 `(ClassLoader,String,Class,ProtectionDomain,byte[])` 与 6 参 `(Module,ClassLoader,String,Class,ProtectionDomain,byte[])`；6 参默认委托 5 参，5 参默认返回 null。

## 2. Java 用户如何使用
```java
instrumentation.addTransformer(new ClassFileTransformer() {
    public byte[] transform(ClassLoader l, String n, Class<?> c, ProtectionDomain pd, byte[] b) {
        return transform(b); // 返回新字节码，或 null 表示不变换
    }
});
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument` 大量 agent 覆写 5 参 transform 返回 null/新字节码。
- ByteBuddy/ASM 生成 agent 覆写 transform 做字节码插桩。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 两个 default transform 方法返回 byte[] 且抛 IllegalClassFormatException。
黑盒：覆写 5 参返回标记字节；6 参默认委托 5 参（返回同标记）；空实现两方法默认返回 null。
对应 `ClassFileTransformerTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 接口面（javap 对照）：JDK-8 仅一个抽象方法 transform(ClassLoader,String,Class,ProtectionDomain,byte[])；JDK-27 增加 default transform(Module,...) 两方法且内部委托 5 参版本（实证：覆写 5 参即可拦截 default 路径调用）。
- 回调契约（真实 agent 闭环）：返回 null = 不修改类文件；计数 transformer 在子 JVM 观察到 >= 1 次（实证 JDK-8 20 次 / JDK-27 16 次）后续类加载。
- 异常路径：transform 抛异常时 JVM 忽略该 transformer（类保持原字节码）——以抛异常 transformer 后子 JVM 正常退出佐证。
- 本侧（JDK-27）：default transform(Module,...) 两变体存在且委托 5 参版本（实证覆写 5 参即可拦截）。
