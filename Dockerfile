FROM openjdk:8-jdk-alpine
VOLUME /tmp
VOLUME /root/input
EXPOSE 3355
COPY target/pilotlb-0.0.1-SNAPSHOT.jar /root/app/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/root/app/app.jar"]


