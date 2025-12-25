# 1. Java 21 JDK 이미지를 기반으로 시작
FROM bellsoft/liberica-openjdk-alpine:21

# 2. 빌드 시 생성되는 jar 파일의 위치를 변수로 설정
ARG JAR_FILE=build/libs/*.jar

# 3. jar 파일을 컨테이너 내부로 복사하고 이름을 app.jar로 변경
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 시작될 때 실행할 명령어 설정
ENTRYPOINT ["java", "-jar", "/app.jar"]