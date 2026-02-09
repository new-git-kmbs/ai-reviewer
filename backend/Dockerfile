FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy only backend sources into the image
COPY backend/ ./

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]
