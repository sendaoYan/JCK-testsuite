# java.lang.invoke.MethodHandles.Lookup 测试设计案（JDK-8）

> 覆盖目标：8→55 断言。在保留原 8 条冒烟（findVirtual/findStatic/findConstructor/findGetter/
> unreflect/in 基本路径）基础上，补齐 find*/unreflect* 家族的访问控制契约与精确异常消息，并锁定
> JDK-8 与 27 的 API 存在性差异。

## 1. API 功能

`MethodHandles.Lookup` 是方法句柄查找上下文：`lookupClass/lookupModes/in/toString`；
查找族 `findVirtual/findStatic/findSpecial/findConstructor/findGetter/findSetter/
findStaticGetter/findStaticSetter/bind`；反反射族 `unreflect/unreflectConstructor/
unreflectGetter/unreflectSetter`；`revealDirect`。访问模式常量 PUBLIC/PRIVATE/PROTECTED/PACKAGE。

## 2. 用户如何使用

```java
MethodHandles.Lookup lk = MethodHandles.lookup();
MethodHandle mh = lk.findVirtual(String.class, "length", MethodType.methodType(int.class));
int n = (int) mh.invoke("abc"); // 3
```
查找失败按成员可见性分别抛 NoSuchMethodException/NoSuchFieldException（签名不符）或
IllegalAccessException（访问不足）；`in(别类)` 降低模式后私有成员不可达。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/invoke/MethodHandles.Lookup.html：访问级别与异常语义）。
- OpenJDK 8u `jdk/src/share/classes/java/lang/invoke/MethodHandles.java`（实测消息来源）。
- JLS 8 §6.6.1（私有访问）、JVMS 5.4.3/5.4.4（方法/字段解析）。

## 4. 测试用例设计（映射到 LookupTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | lookupClass/lookupModes、五大 find 基本路径（原冒烟保留） | testLookup |
| P0 | findVirtual 自造 Sample.size() round-trip 42 + handle 类型全等；缺方法 NSME；私有方法 IAE | testFindVirtualDeep |
| P0 | findStatic 精确值/缺方法 NSME/私有静态 IAE/签名不符 NSME | testFindStaticDeep |
| P0 | findGetter/findSetter 往返 + 普通读可见；findStaticGetter/Setter 静态往返 | testFindGetterSetterDeep |
| P0 | 缺字段 NSFE；类型不符 NSFE 消息 `no such field`；静态字段走实例访问 → IAE `expected a non-static field`；实例字段走静态访问 → IAE `expected a static field`；final 字段 findSetter → IAE `unexpected set of a final field` | 同上 |
| P0 | findSpecial：LOOKUP.in(Sub).findSpecial(Sup.v, Sub.class)==1（invokespecial 语义）；specialCaller 非子类 → IAE `caller class must be a subclass below the method` | testFindSpecialDeep |
| P0 | findConstructor round-trip 字段精确 + 类型 ()Sample；签名不符 NSME；私有构造器 IAE | testFindConstructorDeep |
| P0 | unreflect：与反射同值；私有静态 `member is private` IAE；setAccessible(true) 后 OK、复位后再拒；unreflectConstructor/Getter/Setter；unreflectSetter 静态 final → IAE `unexpected set of a final field`；实例 final 同；静态 final 可读 | testUnreflectDeep |
| P1 | in(String) 降级：modes==PUBLIC、toString 含类名与 public；降级后拒访自身私有静态；in(同类) 保模式；publicLookup 形态 | testInDowngrade |
| P1 | JDK-8 无 privateLookupIn/dropLookupMode/hasPrivateAccess/findClass/accessClass（反射锁定 NoSuchMethodException） | testVersionSpecificAbsences |

## 易错规范细节（实测锁定）

- **JDK-8 无 nest（JEP 181）**：外部类 Lookup 访问自身嵌套类私有成员被拒——findVirtual/findStatic 报
  `no such method`（解析期隐藏），unreflect 报 `member is private`；JDK-11+ 两者都成功（27 侧断言）。
- findGetter/findSetter 与 findStaticGetter/findStaticSetter 之间的 static 错配抛 IAE 而非 NSFE
  （`expected a non-static field`/`expected a static field`）。
- unreflectSetter 对 static final 的拒绝消息：8 侧 `unexpected set of a final field`，27 侧
  `static final field has no write access`——分侧断言。
- in(别包类) 在 8 侧模式恰好只剩 PUBLIC；publicLookup 同。
