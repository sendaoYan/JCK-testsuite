# jdk.dynalink.SecureLookupSupplier 设计案

## 1. API 功能
安全查找供应者：getLookup + 常量。

## 2. Java 用户如何使用
new SecureLookupSupplier(lookup)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/SecureLookupSupplier.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+getLookup+常量。
## 5. 契约深化（JDK-27）
- GET_LOOKUP_PERMISSION_NAME=="dynalink.getLookup"；null lookup→NPE；getLookup 无权限也可调用（无 SecurityManager 场景）；getLookupPrivileged 仅子类可见（Exposing 子类验证）；CallSiteDescriptor 继承关系活体验证；类非 final（可子类化以暴露 protected 视图）。
