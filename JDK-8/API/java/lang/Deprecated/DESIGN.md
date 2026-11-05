# Deprecated — 设计方案

## 1. API 功能
`java.lang.Deprecated`（注解，JDK-8 侧**无成员**）：标记已过时程序元素。@Retention(RUNTIME)、@Documented、@Target({CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})（7 值，无 MODULE）。

## 2. Java 用户如何使用
```java
@Deprecated
void old() {}
// 编译器 -Xlint:deprecation 警告；运行时反射可回读
```

## 3. 参考来源
javap -public java.lang.Deprecated（8 侧空接口）；JDK 9+ javadoc（since/forRemoval，8 缺失）。

## 4. 测试用例设计 + 易错规范细节
- **8 侧 getDeclaredMethods().length == 0**（javap 确认 + 反射白盒断言）；27 侧为 2（MODERN 分支不适用于 8 侧文件，8 侧直接断 0）。
- @Target 精确 7 值（EnumSet 全含断言 + MODULE 缺席——ElementType.valueOf("MODULE") 在 8 抛 IAE，try/catch 断缺席）。
- 无成员注解代理：hashCode==0、toString "@java.lang.Deprecated()"、两次查找 equals true。
- 真实 API 差异（本批实测）：java.util.Date.getDate() 双侧 @Deprecated；**8 侧 Object.finalize 与 Class.newInstance 均无 @Deprecated 注解**（分别于 9/9 加入——27 侧 forRemoval/since 各值见 27 侧 DESIGN）。
- DeprecatedTest.java 现有 PASS 断言：42。
