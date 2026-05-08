FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /my-blog-back-app
COPY pom.xml .

RUN mvn dependency:go-offline dependency:resolve-plugins --no-transfer-progress

COPY src ./src

RUN mvn package -DskipTests --no-transfer-progress

FROM tomcat:10.1
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /my-blog-back-app/target/my-blog.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
