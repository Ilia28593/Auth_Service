FROM openjdk:17
ARG JAR_FILE=target/*.jar
EXPOSE 8082
ADD ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","/application.jar"]
