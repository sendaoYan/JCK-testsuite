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
## 深化批次（2026-11-05）

新增 Part 3 深化（69→104 断言）：testAccessMessages（NSFE `no such field`/IAE
`expected a non-static field`×2/`expected a static field`/`unexpected set of a final field` 精确消息）、
testUnreflectAccessMessages（nestmate 私有 OK；publicLookup 与 in(String) 拒绝均 `member is private`；
unreflectSetter 静态 final → IAE `static final field has no write access`（**JDK-9+ 专用措辞，8 为
`unexpected set of a final field`**）；实例 final 同措辞拒绝；静态 final 可读）、
testNestPrivateAccess（**nest（JDK-11+）**：findVirtual/findGetter/findConstructor 直达自身嵌套类
私有成员并 round-trip——8 侧为 IAE，核心版本差异）、testPrivateLookupInDeep（null 目标 NPE；
String 目标 → IAE `module java.base`（跨模块未 open）；Holder 提权后 hasPrivateAccess/
hasFullPrivilegeAccess、**同模块 previousLookupClass==null（无模块跳转不记录）**、可回读前类私有静态
hiddenMark；null lookup NPE）、testDropLookupModeExact（full==95；drop(PACKAGE)==17、drop(PRIVATE)==25、
**drop(MODULE)==1（塌缩为仅 PUBLIC，任务书预期外实测值）**、drop(PUBLIC)==0；previousLookupClass
语义）、testEnsureInitializedDeep（ensureInitialized 恒等返回、accessClass 公共类、findClass FQCN
解析与 CNFE）。
