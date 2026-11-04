# java.util.jar 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面去掉 Pack200（JDK-14 移除，反射锁定 ClassNotFoundException），加 multi-release jar API（isMultiRelease/getVersion/baseVersion/runtimeVersion/versionedStream/(File,boolean,Version) ctor）。

## 2. Java 用户如何使用

非 MR jar：getVersion()==baseVersion()（major 8）；runtimeVersion major>8；versionedStream 数量==stream。

## 3. 参考来源

JEP 238 Multi-Release JAR Files；JDK-14 Pack200 移除公告。

## 4. 测试用例设计

JDK-8 拷贝基础 + JarFile.testJdk27Additions() 多版本块 + Attributes MULTI_RELEASE 常量 + JarException Pack200 缺席探针 + missing file NoSuchFileException（与 JDK-8 FileNotFoundException 差异锁定）。
