# java.lang.invoke.MethodHandles.Lookup — 设计方案

## 1. API 功能
方法句柄查找上下文（public static final 嵌套类）：`lookupClass/lookupModes/hasFullPrivilegeAccess`、`in/dropLookupMode/previousLookupClass/privateLookupIn`、`findVirtual/findStatic/findSpecial/findConstructor`、`findGetter/findSetter/findStaticGetter/findStaticSetter/findVarHandle`、`unreflect*`、`revealDirect`、`defineClass/defineHiddenClass`、`toString`。

## 2. Java 用户如何使用
```java
MethodHandles.Lookup lk = MethodHandles.lookup();
MethodHandle m = lk.findVirtual(String.class, "length", MethodType.methodType(int.class));
int n = (int) m.invoke("hi"); // 2
```

## 3. 开源代码实际用法调研
- JDK `MethodHandles.Lookup`（900+ 行 Javadoc + AccessFlag 语义）。
- JDK `test/jdk/java/lang/invoke/lookup/`（AccessModesTest/PrivateLookupTest/ReflectiveLookupTest）。
- LambdaMetafactory/记录反序列化器均以 lookup 为首参做能力校验。

## 4. 测试用例设计 + 自动化实现
白盒：嵌套类归属、modes()=95(publicLookup=32)、方法存在性。
黑盒：findVirtual/findStatic/findConstructor 精确调用；getter/setter/静态/VarHandle 全套；bind、unreflect、revealDirect 还原 MemberName；in("java.lang") 后 modes==1；dropLookupMode(PRIVATE)→25/(PROTECTED)→27/(MODULE)→1；privateLookupIn 提权；私有成员跨类拒绝（NoSuchMethod/NoSuchField/IllegalAccess）；toString 含 class/modes。
对应 `LookupTest.java` 的 `testBlackBox()`。