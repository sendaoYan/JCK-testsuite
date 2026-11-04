# java.util.prefs 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

Preferences 键值树：put/get/remove/clear/keys 有序、node 树/级联 removeNode、export/import XML round-trip、变更监听（异步分发）、MAX_KEY_LENGTH=80/MAX_VALUE_LENGTH=8192 IAE。

## 2. Java 用户如何使用

隔离 "/jck/test*" 节点 + finally removeNode；事件经 flush/sync 派发后再断言；未注册监听移除 IAE。

## 3. 参考来源

Preferences javadoc（FileSystemPreferences 后端）。

## 4. 测试用例设计

9 个测试各 testContracts()。
