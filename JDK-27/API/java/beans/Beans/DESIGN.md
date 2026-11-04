# java.beans.Beans — 设计案

## 1. API 功能
Bean 工具类。isDesignTime/isGuiAvailable 及对应 setter、instantiate（按类名/Class）、isInstanceOf/getInstanceOf。

## 2. Java 用户如何使用
`Beans.isDesignTime()` 判断设计期；`Beans.instantiate(cl, "pkg.Bean")` 动态创建 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `Beans.java`；IDE 设计工具使用。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：designTime/guiAvailable 开关、isInstanceOf/getInstanceOf、instantiate 两种重载。
