FROM maven:3.9.6-amazoncorretto-21 as build
WORKDIR /app

COPY pom.xml .
COPY ewm-stats/pom.xml ./ewm-stats/
COPY ewm-stats/client/pom.xml ./ewm-stats/client/
COPY ewm-stats/dto/pom.xml ./ewm-stats/dto/
COPY ewm-stats/stats/pom.xml ./ewm-stats/stats/

COPY . .

RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/ewm-stats/stats/target/stats-*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
