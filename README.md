# kkFileViewOfficeEdit
对 https://github.com/kekingcn/kkFileView/ 进行了二次开发。整合了openOffice进来，项目体积更大了！但使用和配置更简单。

打包方式改为了WAR包部署。

整合了pageOffice进行在线文档编辑。

### 在线预览使用方法
IP+端口+项目名/onlinePreview?url=你的文件地址

更详细的说明上面见那个地址，注意传递的是地址，不要下载流

### 文档编辑使用方法
IP+端口+项目名/onlineEdit?url=你的文件地址&callBack=回调地址

保存文档成功后，会触发回调，onlineEdit方法调用你传递来的回调地址，并把重新生成的文档下载地址传回去（参数名：downloadPath）
考虑到有可能两个项目部署到不同服务器，文件不互通，所以采用这样的方式

编辑地址示例：

http://127.0.0.1:8012/onlineEdit?callBack=localhost:8080/ZsContent/saveCallBack&url=http%3A%2F%2Flocalhost%3A8080%2F1.doc&fileId=yourId

url需要进行encode！ 回调地址不能带HTTP：// ！
如果还有其它参数，可以拼接到URL后面

回调地址示例：
http://localhost:8080/ZsContent/saveCallBack?downloadPath=http://127.0.0.1:8012/download?filename=D:/Users/chenkailing/test/a3ba6f7c-a989-4f47-8173-f795ff30a92b.doc&fileId=123456

如果还有其它参数也会一并传回

### 项目特性

1. 支持office，pdf等办公文档
1. 支持txt,java,php,py,md,js,css等所有纯文本
1. 支持zip,rar,jar,tar,gzip等压缩包
1. 支持jpg，jpeg，png，gif等图片预览（翻转，缩放，镜像）
1. 使用spring boot开发，预览服务搭建部署非常简便
1. rest接口提供服务，跨平台特性(java,php,python,go,php，....)都支持，应用接入简单方便
1. 抽象预览服务接口，方便二次开发，非常方便添加其他类型文件预览支持
1. 整合了pageOffice进行在线文档编辑
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



1. 第一步：pull项目https://github.com/yijiusmile/kkFileViewOfficeEdit.git

2. 第二步：配置redis地址，如
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

