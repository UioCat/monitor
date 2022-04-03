FROM tomcat:latest
MAINTAINER hanxun<406453373@qq.com>
WORKDIR /usr/local
ADD monitor.war /usr/local/tomcat/webapps/monitor.war
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
