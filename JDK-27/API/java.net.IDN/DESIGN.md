# IDN 设计文档

## 一、API 功能

国际化域名转换，把 Unicode 域名编码为 ASCII(Punycode) 及反向解码。

## 二、Java 用户如何使用

用户用 IDN.toASCII(unicode)/IDN.toUnicode(ascii)，可带 ALLOW_UNASSIGNED/USE_STD3_ASCII_RULES 标志。

## 三、开源代码实际用法调研

支持中文/多语言域名的注册商、浏览器、邮件系统使用。

## 四、测试用例设计

toASCII/toUnicode 往返一致；中文域名编码为 xn-- 前缀；USE_STD3_ASCII_RULES 标志行为；非法字符抛异常。
