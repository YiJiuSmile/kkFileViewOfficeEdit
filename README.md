# kkFileViewOfficeEdit
对 https://github.com/kekingcn/kkFileView/ 进行了二次开发,整合了openOffice进来，项目体积更大了！但使用更简单。
打包方式改为了WAR包部署。
整合了pageOffice进行在线文档编辑。


### 项目特性

1. 支持office，pdf等办公文档
1. 支持txt,java,php,py,md,js,css等所有纯文本
1. 支持zip,rar,jar,tar,gzip等压缩包
1. 支持jpg，jpeg，png，gif等图片预览（翻转，缩放，镜像）
1. 使用spring boot开发，预览服务搭建部署非常简便
1. rest接口提供服务，跨平台特性(java,php,python,go,php，....)都支持，应用接入简单方便
1. 抽象预览服务接口，方便二次开发，非常方便添加其他类型文件预览支持
1. 最最重要Apache协议开源，代码pull下来想干嘛就干嘛

### 文档预览效果
> Excel预览效果

![输入图片说明](https://gitee.com/uploads/images/2017/1213/093051_cd55b3ec_492218.png "屏幕截图.png")
> doc预览效果

![输入图片说明](https://gitee.com/uploads/images/2017/1213/092350_5b2ecbe5_492218.png "屏幕截图.png")

> zip,rar压缩预览效果

![输入图片说明](https://gitee.com/uploads/images/2017/1213/093806_46cede06_492218.png "屏幕截图.png")

> png,jpeg,jpg等图片预览效果，支持滚轮缩放，旋转，倒置等

![输入图片说明](https://gitee.com/uploads/images/2017/1213/094335_657a6f60_492218.png "屏幕截图.png")
考虑说明篇幅原因，就不贴其他格式文件的预览效果了，感兴趣的可以参考下面的实例搭建下



1. 第一步：pull项目https://github.com/kekingcn/file-online-preview.git

2. 第二步：配置redis地址和OpenOffice目录，如
```
#=============================================#spring Redisson配置#===================================#
spring.redisson.address = 192.168.1.204:6379
##资源映射路径(因为jar方式运行的原因)
file.dir = C:\\Users\\yudian\\Desktop\\dev\\

```
file.dir为转换文件实际存储地址，注意要以/结尾

3. 第三步：运行FilePreviewApplication的main方法，服务启动后，访问http://localhost:8012/
会看到如下界面，代表服务启动成功
![输入图片说明](https://gitee.com/uploads/images/2017/1213/100221_ea15202e_492218.png "屏幕截图.png")

