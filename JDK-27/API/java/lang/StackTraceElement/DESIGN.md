# StackTraceElement — 设计方案

## 1. API 功能
`java.lang.StackTraceElement`（final，`Serializable`）表示一个栈帧，两种构造器（4 参/7 参），getter：
`getClassName/getMethodName/getFileName/getLineNumber/isNativeMethod/getModuleName/getModuleVersion/getClassLoaderName`，另 `toString/equals/hashCode`。

## 2. Java 用户如何使用
- 通常由 `Throwable.getStackTrace()` 返回；也可手工构造用于 `Throwable.setStackTrace`。
```java
for (StackTraceElement e : new Throwable().getStackTrace()) {
    System.out.println(e.getClassName() + "." + e.getMethodName() + ":" + e.getLineNumber());
}
```

## 3. 开源代码实际用法调研
- 日志框架解析 `StackTraceElement` 生成调用点；JDK `test/jdk/java/lang/StackTraceElement` 验证 `toString` 格式（`(file:line)`/`(Native Method)`/`(Unknown Source)`）与 `equals/hashCode`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 final、`Serializable`、两个构造器存在。
黑盒：4 参 getter（模块/加载器信息为 null）；7 参顺序 classLoaderName/moduleName/moduleVersion/className/methodName/fileName/lineNumber；`toString` 三种格式；`isNativeMethod` 对 line -2；值语义 `equals/hashCode`。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- fillInStackTrace 真帧闭环（className/methodName/lineNumber/isNativeMethod/fileName/toString 形态/`:N` 与 getLineNumber 一致）。
- 真帧模块信息（JDK-9+ 面）：classpath 类（无名模块）帧 `getModuleName()==null`、`getModuleVersion()==null`、`getClassLoaderName()=="app"`；java.base 帧（`ArrayList.forEach` 内捕获）`getModuleName()=="java.base"`、`getClassLoaderName()==null`（bootstrap）、`getModuleVersion()!=null`、toString 形如 `java.base/java.util.ArrayList.forEach(ArrayList.java:NNN)`（模块限定前缀，JDK-8 无此形态）。
- 7 参构造器 equals 语义：7 参(null,null,null,…)**等于**同四元组 4 参构造实例（hash 也等）；module/loader/version 任一非空即不等。
- getStackTrace 防御性拷贝、setStackTrace（拷贝/null NPE/空数组）、构造器空参守卫（NPE `"Declaring class is null"`/`"Method name is null"`，与 JDK-8 逐字一致）。
- 行号语义与 toString 全矩阵、equals/hashCode 全契约（同 JDK-8 文件，双侧实测一致）。
- serialVersionUID=6992337162326171013L（ObjectStreamClass.lookup 锁定，与 JDK-8 相同值）；真帧序列化往返保持含模块信息在内的全部值。

### 使用方式
- 同前：单一 main 类 + `check` 断言计数；`testCallerChainFrames` 必须由 main 直接调用（st[1] 才是 main）。

### 参考来源
- Oracle Java SE 27 javadoc（StackTraceElement 模块信息/equals 契约）。
- 本仓库双 JDK 实测探针（/tmp/lcI-exp）。

### 测试用例设计 + 易错规范细节
- 新增方法：与 JDK-8 侧同名集合 + testRealFrameModuleInfo、testJavaBaseFrameModuleInfo、testSevenArgEqualsSemantics。
- 易错点：java.base 帧的 toString 带 `java.base/` 前缀而无名模块帧不带（即便 loader 名为 "app"）；负行号（非 -2）toString 省略 `:N`；`setStackTrace(null)` 侧抛 helpful NPE（消息含 JVM 内部细节，只锁类型不锁消息）。
- 版本差异锁定：getModuleName/getModuleVersion/getClassLoaderName/7 参构造器为 27 独有；共有面（4 参行为、equals/hashCode、toString 矩阵、NPE 消息、serialVersionUID）双侧逐字一致。
