# java.security.SecureClassLoader 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(ClassLoader)`、`(String, ClassLoader)`（均 protected，子类调用）
- protected：`defineClass(String, byte[], int, int, CodeSource)`、`defineClass(String, ByteBuffer, CodeSource)`、`getPermissions(CodeSource)`

## 2. Java 用户如何使用
签名 jar 场景为类绑定 CodeSource/保护域；JDK-27 getPermissions 默认返回空集合（不再按 URL 授权）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/SecureClassLoader.java`（getPermissions 返回 new Permissions()）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/instrument/BufferClassLoader.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessibleObject/CanAccessTest.java`（自定义 SecureClassLoader）

## 4. 测试用例设计 + 自动化实现
- 正常：子类化 3 个构造器，命名加载器 getName/getParent/委托加载
- 组合：public 覆写 getPermissions 暴露默认行为——默认空集合（不授 FilePermission，JDK-27 变更）；定制覆写可授予任意权限
- 边界：defineClass 由白盒反射验证存在（protected final）

断言入口：`SecureClassLoaderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 追加契约维度（深化批）
- null CodeSource：默认 getPermissions 返回非空**空授权**集合。
- 委托身份：loadClass("java.lang.String")==String.class（数组描述符不被 loadClass 支持，勿断言）。
- 结构：恰 2 个 protected defineClass 重载；getPermissions protected；类非 final。
