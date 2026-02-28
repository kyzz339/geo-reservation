FROM eclipse-temurin:21-jre-jammy
# 빌드된 모듈의 jar 파일을 복사
# -plain.jar를 제외하고 실행 가능한 fat jar만 복사
ARG JAR_FILE=web/build/libs/*[!plain].jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]
