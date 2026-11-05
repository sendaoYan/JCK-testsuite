# jdk.jshell.Wrap（内部）设计案

## 1. API 功能
源码包装器族（GeneralWrap 契约：wrapped/索引与行号双向映射）；Range 是字符区间；CompoundWrap 是组合包装。

## 2-3. 略（包私有）

## 4. 测试设计 + 易错
- Range：int begin/end、(int,int) 与 (String) 双 ctor、length=end-begin、begin==end isEmpty、toString "Range[b,e)"、无 equals 覆写（恒等语义）。
- Wrap 静态工厂 methodWrap 等输出 wrapped() 含方法体；反射调用需 setAccessible + --add-opens。
