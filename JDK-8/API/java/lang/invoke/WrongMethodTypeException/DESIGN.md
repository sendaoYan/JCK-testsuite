# java.lang.invoke.WrongMethodTypeException — 设计方案

## 1. API 功能
句柄调用类型不匹配时抛出的 RuntimeException：仅有 () 和 (String) 两个 public 构造（无 cause 构造，但 initCause 可用一次）；经 Throwable 可序列化。真实触发源：invokeExact 静态签名不匹配、invokeWithArguments 元数不符、asType 不可能转换等。

## 2. Java 用户如何使用
```java
try {
    int r = (int) MethodHandles.identity(int.class).invokeExact("x");
} catch (WrongMethodTypeException e) {
    // 8: expected (int)int but found (String)int
    // 27: handle's method type (int)int but found (String)int
}
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/WrongMethodTypeException.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/WrongMethodTypeException.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/WrongMethodTypeException.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/WrongMethodTypeException.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/WrongMethodTypeException
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造链 ()/("m")：message null/精确；恰好 2 个 public 构造；直接 RuntimeException 子类；Serializable | testConstructorChain |
| P0 | 序列化 round-trip：类与 message 保留 | testSerializationRoundTrip |
| P0 | initCause 一次成功（返回 this、cause 恒等）、二次 → ISE "Can't overwrite cause" | testInitCauseRules |
| P0 | 真实触发三个：invokeExact 错型（8/27 消息前缀不同，公共子串 "but found (String)int"）、元数（"cannot convert MethodHandle(int)int to (Object,Object,Object)Object" 双侧一致）、asType 不可能转换 | testRealTriggers |
| P1 | Throwable 默认面：toString 两种形态、suppression 默认开启、serialVersionUID 字段存在 | testThrowableDefaults |

易错点：
- 任务书提到的 (String,Throwable) 构造不存在（javap 双侧确认只有 2 个 public 构造），以 javap 为准。
- invokeExact 触发消息 8/27 措辞不同：8 "expected X but found Y"、27 "handle's method type X but found Y"。
