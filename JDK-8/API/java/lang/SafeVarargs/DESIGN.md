# SafeVarargs — 设计方案

## 1. API 功能
`java.lang.SafeVarargs`（注解，无成员）：断言 varargs 方法不以其可变参数数组做不安全操作，抑制调用方 heap pollution 警告。@Retention(RUNTIME)、@Documented、@Target({CONSTRUCTOR, METHOD})。仅可标注 static/final/private 方法与构造器（编译期限制）。

## 2. Java 用户如何使用
```java
@SafeVarargs
static final <T> List<T> asList(T... a) { return Arrays.asList(a); }
```

## 3. 参考来源
JLS 9.6.4.7；javap -public 双侧同构。

## 4. 测试用例设计 + 易错规范细节
- @Target 恰 {CONSTRUCTOR, METHOD}（**顺序锁定**：CONSTRUCTOR 在前）。
- 三种合法标注处（static final 方法/private final 方法/构造器）反射回读非 null。
- 无成员代理：hashCode==0、toString "@java.lang.SafeVarargs()"。
- **反射调用 varargs 方法必须传打包数组** `m.invoke(t, (Object) new Object[]{...})`；散传两侧抛 IAE（8 "wrong number of arguments" / 27 "...: 3 expected: 1"）——踩坑记录。
- SafeVarargsTest.java 现有 PASS 断言：35。
