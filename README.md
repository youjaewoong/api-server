### swagger
- <http://localhost:9001/cdp/swagger-ui/index.html#/>

### swagger json
- <http://localhost:9001/v3/api-docs>

### sample ddl
```
ms-SQL
CREATE TABLE SAMPLE (
	ID int IDENTITY (1, 1) NOT NULL,
	NAME varchar(10) COLLATE Korean_Wansung_CI_AS  NULL,
	TITLE varchar(20) COLLATE Korean_Wansung_CI_AS  NULL,
	CONTENTS varchar(200) COLLATE Korean_Wansung_CI_AS NULL,
	TYPE varchar(10) NULL,
	REG_DT date ,
	EDIT_DT date
);

postgreSQL
CREATE TABLE SAMPLE (
    ID SERIAL PRIMARY KEY, 
    NAME VARCHAR(10) , 
    TITLE VARCHAR(20) ,
    CONTENTS VARCHAR(200),
    TYPE VARCHAR(10),
    REG_DT DATE,
    EDIT_DT DATE
);

```
### 실행옵션 (필수)
```
intellij 
 > Run
  > Edit Configurations...
   > Active profiles : local 추가
```
### build
```
#1 build 
gradlew clean bootJar

#2 build
gradlew clean build -x test
```
### 프로파일별 실행
```
#1 local : 9001
java -jar -Dspring.profiles.active=local api-application.jar

#2 dev : 9002
java -jar -Dspring.profiles.active=dev api-application.jar

#3 prod : 9003
java -jar -Dspring.profiles.active=prod api-application.jar
```

### actuator
- <http://localhost:9001/actuator/health>
- <http://localhost:9001/actuator/metrics/hikaricp.connections.active>
