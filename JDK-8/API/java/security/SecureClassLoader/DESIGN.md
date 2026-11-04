# SecureClassLoader — 设计案

## 1. API 功能
安全类加载器基类：protected 构造 ()/(ClassLoader)（JDK-27 增 (String, ClassLoader)）+ protected final defineClass(String,byte[],int,int,CodeSource)/(String,ByteBuffer,CodeSource) + protected getPermissions(CodeSource)。extends ClassLoader，非 final。

## 2. 使用方式
子类覆写 getPermissions 授予 CodeSource 权限；defineClass 关联 ProtectionDomain。JDK-8 仅有两个 protected 构造器（无命名构造器、无 getName()——与 27 的分叉）。

## 3. 参考来源
- javadoc `java.security.SecureClassLoader`。
- 双侧探针（默认 getPermissions 空、null CodeSource 容忍）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：(ClassLoader) 精确设父；null 父 → getParent()==null；默认构造子类的父 == 定义子类的加载器。
- 委托：loadClass("java.lang.String")==String.class 等身份判定（**数组描述符 "[L...;" 不被 loadClass 支持——实测 CNFE，勿断言**）。
- **默认 getPermissions(CodeSource) 返回空授权集（JDK-8 实测无 FilePermission 位置授权、无 SecurityPermission）**；null CodeSource → 非空集合且空授权；覆写子类收到调用方 CodeSource 身份并可行使宽授权（AllPermission implies）。
- 结构：恰 2 个 protected defineClass 重载；getPermissions 为 protected；类非 final 可子类化。
- 易错点：JDK-8 无 ClassLoader.getName()；getPermissions 是子类钩子非公开 API。
