# java.lang.constant.DirectMethodHandleDesc.Kind — 设计方案

## 1. API 功能
枚举（11 常量）+ public 字段 `refKind`/`isInterface` + 静态 `valueOf(int)`/`valueOf(int,boolean)`；把 JVMS 方法句柄 kind 映射为可读名。

## 2. Java 用户如何使用
```java
Kind k = Kind.valueOf(9);           // INTERFACE_VIRTUAL
DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(k, owner, name, mtd);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/DirectMethodHandleDesc.java`（Kind 内部 TABLE 查找）。
- MethodHandleInfo.ReferenceKind 的同名映射。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、Enum 父类、11 常量、public refKind/isInterface 字段类型、valueOf 重载。
黑盒：声明顺序；refKind/isInterface 全表（STATIC 6、INTERFACE_STATIC 6i、VIRTUAL 5、INTERFACE_VIRTUAL 9i、SPECIAL 7、INTERFACE_SPECIAL 7i、CONSTRUCTOR 8、GETTER 1、SETTER 3、STATIC_GETTER 2、STATIC_SETTER 4）；valueOf(String) 往返+IAE；valueOf(int) 各值；valueOf(int,boolean) 区分 STATIC/INTERFACE_STATIC、SPECIAL/INTERFACE_SPECIAL；0/-1/10 IAE；compareTo。
对应 `KindTest.java` 的 `testBlackBox()`。
