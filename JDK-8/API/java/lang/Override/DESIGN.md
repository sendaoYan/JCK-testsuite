# Override — 设计方案

## 1. API 功能
`java.lang.Override`（注解，无成员）：指示方法覆写超类/实现接口声明。@Retention(**SOURCE**)、@Target({METHOD})——仅编译期检查，class 文件不留存。

## 2. Java 用户如何使用
```java
class Sub extends Base { @Override void m() {} }  // 签名不匹配时编译错误
```

## 3. 参考来源
JLS 9.6.4.4；javap -public 双侧同构。

## 4. 测试用例设计 + 易错规范细节
- **SOURCE retention → 运行时任何 @Override 站点 getAnnotation(Override.class)==null**（超类覆写/接口实现/抽象实现/toString 覆写四场景逐一锁定）。
- @Target 恰 {METHOD} 单值；@Documented 不在 Override 上；元注解恰 Retention+Target。
- 覆写 dispatch 本身正常（运行时注解缺失不影响方法绑定）。
- OverrideTest.java 现有 PASS 断言：30。
