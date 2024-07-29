# FROM = base image
FROM openjdk:17-slim
# *T.jar를 app.jar로 복사 (내꺼에서 이미지로 복사하기)
COPY build/libs/*T.jar app.jar
# RUN이라는 명령어
RUN ["java","-version"]
#ENV profiles prod
# run이 되면 이 명령어들을 실행하겠다
CMD ["java","-jar","-Dspring.profiles.active=${profiles}","app.jar"]
EXPOSE 8080
