FROM openjdk:17
EXPOSE 12345
COPY target/BackEnd-2.0.jar /app/BackEnd.jar
ENTRYPOINT ["nohup", "java", "-jar", "/app/BackEnd.jar", "&"]