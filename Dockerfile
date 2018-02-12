# Use latest Maven as base docker image
FROM maven

COPY pom.xml /pom.xml
COPY src /src
#RUN mvn install

# define run command
CMD ["mvn", "spring-boot:run"]

