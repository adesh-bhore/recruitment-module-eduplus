#FROM tomcat:8.5.90-jdk8
FROM --platform=linux/arm64 tomcat:8.5.100-jdk8
RUN mv webapps webapps2
RUN mv webapps.dist/ webapps
RUN rm -rf /usr/local/tomcat/webapps/*ROOT
COPY build/libs/ROOT.war /usr/local/tomcat/webapps/
#COPY server.xml /usr/local/tomcat/conf/
#COPY web.xml /user/local/tomcat/conf/
#COPY *.pem /usr/local/tomcat/conf/
COPY java.security /opt/java/openjdk/jre/lib/security/
ENV TZ="Asia/Calcutta"
EXPOSE 443 8443 587 25 465
CMD ["catalina.sh","run"]
