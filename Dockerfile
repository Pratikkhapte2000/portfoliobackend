FROM amazoncorretto:17
WORKDIR /home/app
COPY build/libs/portfolio-tracker-0.0.1-SNAPSHOT.jar /home/app/application.jar
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /home/app/application.jar