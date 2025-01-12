# Base image olarak OpenJDK kullanıyoruz
FROM openjdk:17-jdk-slim

# Uygulama için bir çalışma dizini oluştur
WORKDIR /app

# Maven veya Gradle ile oluşturulmuş JAR dosyasını kopyala
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]