#FROM openjdk:8-jdk-alpine
FROM maven
VOLUME /tmp
VOLUME /root/input
EXPOSE 3355
#COPY target/pilotlb-0.0.1-SNAPSHOT.jar /root/app/app.jar
COPY ./ /root/src/
RUN cd /root/src
RUN mvn clean package
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/root/app/app.jar"]

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","target/pilotlb-0.0.1-SNAPSHOT.jar"]
