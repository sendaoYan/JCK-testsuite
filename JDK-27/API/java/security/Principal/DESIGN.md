# java.security.Principal 黑盒行为测试设计

## 1. API 功能
- 方法：`getName()`、`implies(Subject)`（default false）、equals/hashCode/toString

## 2. Java 用户如何使用
Subject/X500Principal/证书主体统一接口。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Principal.java`（default implies 返回 false）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/javax/security/auth/Subject/`（Principal 集合操作）

## 4. 测试用例设计 + 自动化实现
- 黑盒：自实现 Principal 断 getName 透传与 default implies(任何 Subject)==false；与 Subject 主体集组合（add 后读回同一实例）

断言入口：`PrincipalTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
