# DOMImplementationRegistry — 设计案

## 1. API 功能
DOM 实现引导注册表：newInstance() + getDOMImplementation(List)。

## 2. 使用方式
org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance()。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
newInstance() 引导（读 org.w3c.dom.DOMImplementationSourceList 系统属性，未设则默认接线 Xerces core impl，只读不改全局）；getDOMImplementation 全 feature 串矩阵（Core/XML/LS/Traversal/Range/Events/MutationEvents 命中、CSS/HTML/bogus null、null → NPE、组合 token "Core 3.0 LS 3.0" 命中、含未知 token 失败）；getDOMImplementationList 非空/空表契约；registry 本身不是 DOMImplementationSource（编译期不可 cast）。
