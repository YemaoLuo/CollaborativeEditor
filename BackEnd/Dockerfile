FROM openjdk:17
EXPOSE 12345
COPY target/BackEnd-1.0.jar.jar /app/BackEnd.jar
WORKDIR /app
CMD ["nohup", "java", "-jar", "BackEnd.jar", "&"]