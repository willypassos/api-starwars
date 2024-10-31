## 1. Imagem base do Maven com JDK 17 para compilar a aplicação
#FROM maven:3.8.5-openjdk-17 AS build
#
## 2. Define o diretório de trabalho dentro do container
#WORKDIR /app
#
## 3. Copia o arquivo pom.xml para dentro do container (para baixar dependências)
#COPY pom.xml .
#
## 4. Baixa todas as dependências sem compilar o código
#RUN mvn dependency:go-offline
#
## 5. Copia o código fonte do projeto para o container
#COPY src ./src
#
## 6. Compila o projeto e empacota o JAR (pulando testes)
#RUN mvn clean package -DskipTests
#
## 7. Usando uma imagem mais leve apenas para rodar o JAR (para produção)
#FROM openjdk:17-jdk-slim
#
## 8. Define o diretório de trabalho no container de runtime
#WORKDIR /app
#
## 9. Copia o JAR gerado na etapa de build para a imagem final
#COPY --from=build /app/target/*.jar app.jar
#
## 10. Verifica se o JAR foi copiado corretamente
#RUN ls /app
#
## 11. Expõe a porta 8080 para que a aplicação possa ser acessada
#EXPOSE 8080
#
## 12. Define o comando que será executado quando o container iniciar
#CMD ["java", "-jar", "app.jar"]
