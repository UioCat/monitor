# 拉去镜像
FROM tomcat:9.0.63-jre8-temurin-focal
MAINTAINER hanxun<406453373@qq.com>
# 容器进入后的文件夹
WORKDIR /usr/local
# 将war包放入镜像内
ADD ./target/monitor.war /usr/local/tomcat/webapps/monitor.war
# 挂载卷
VOLUME ["/usr/local/tomcat/webapps", "/usr/local/tomcat/logs"]
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 暴露端口
EXPOSE 8080