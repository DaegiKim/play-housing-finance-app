# play-housing-finance-app *(for KakaoPay)*

#### 목차 (Table of Contents)
1. [설치 및 실행 방법 (How to install and run)](#how-to-install-and-run)
2. [REST API 명세서 (REST API Specification)](#rest-api-rest-api-specification)
    1. [JWT(Json Web Token) 기능](#1-jwtjson-web-token)
    2. [주택금융 공급현황 분석 서비스](#2)
    3. [에러 메시지(Error Messages)](#3-error-messages)
3. [프로젝트 주요 패키지 설명 (Package description)](#package-description)
4. [사용한 주요 오픈소스 목록 (List of used open source libraries)](#list-of-used-open-source-libraries)
5. [데이터베이스 다이어그램 (Database diagram)](#database-diagram)

#### 주택 금융 서비스 API 개발

- 개발환경
    - Java 1.8 +
    - Play Framework 2.7.3 (Java)
    - H2 Database (In-Memory) 
    - sbt
- 문제 해결 전략
	- 금융 기관 목록을 저장하는 Bank 테이블, 기관의 월별 지원 금액을 저장하는 Finance 테이블을 정의하고, 두 테이블이 One-To-Many 관계를 가지도록 함. (하나의 Bank는 다수의 Finance를 가진다.)
	- 주어진 문제를 해결함에 있어, SQL Native Query를 작성하지 않고, JPA와 ORM, Java Collections, Stream API 등을 이용해 Java 언어 레벨에서 계산 하도록 구현.
	- 인증을 위해 JWT을 구현하는 부분은 오픈소스 [jjwt](https://github.com/jwtk/jjwt)와 Play Framework의 기능인 [Action composition](https://www.playframework.com/documentation/2.7.x/JavaActionsComposition)기능을 적용해 해결.
	- 선택 문제(지원금액 예측)에 대해서는 '과거의 데이터가 이후에도 영향을 미칠것이다'라는 가설을 세우고, 이를 간단하게 구현할 수 있는 시계열(time series)을 적용하기로 결정. 시계열을 Java로 구현한 오픈소스 라이브러리 [com.github.signaflo % timeseries % 0.4](https://github.com/signaflo/java-timeseries)를 사용하여 해결함.

---

# 설치 및 실행 방법 (How to install and run)
```bash
# sbt가 없다면 먼저 설치해주세요.
$ brew install sbt@1

# 프로젝트를 내려 받습니다.
$ git clone https://github.com/DaegiKim/play-housing-finance-app.git

# 프로젝트 디렉토리로 이동합니다.
$ cd play-housing-finance-app

# 프로젝트를 실행합니다.
$ sbt run
```
서비스가 정상적으로 구동 되면 http://localhost:9000 으로 접속이 가능합니다.

---

# REST API 명세서 (REST API Specification)

## 1. JWT(Json Web Token) 기능
> API 인증을 위해 JWT(Json Web Token)를 이용해서 Token 기반 API 인증 기능을 개발하고 각 API 호출 시에 HTTP Header 에 발급받은 토큰을 가지고 호출하세요.
### 사용자 등록 (signUp)
> signup 계정생성 API: 입력으로 ID, PW 받아 내부 DB 에 계정 저장하고 토큰 생성하여 출력
#### [Request]
```http
POST /api/auth/signup HTTP/1.1
Host: localhost:9000
Content-Type: application/json

{
  "username":"rmrhtdms",
  "password":"1234"
}
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zMSIsInBhc3N3b3JkIjoiJDJhJDEwJHlGYnFONmFmTVZnYWxwLmVVaWhLTXVNb2ZlRWljRFRSS21HYlZ2TGdURk5XTWoyc0FKVGxPIiwiaWF0IjoxNTY2MTMxMjM2LCJleHAiOjE1NjYxMzE4MzZ9.xiizdvWWKQDqEmkMiOTZVSFuOGlIrTz12d0HVdtkrec",
  "issued_at": "Sun Aug 18 21:27:16 KST 2019",
  "expires_in": "Sun Aug 18 21:37:16 KST 2019"
}
```

### 로그인 (signIn) 
> signin 로그인 API: 입력으로 생성된 계정 (ID, PW)으로 로그인 요청하면 토큰을 발급한다.
#### [Request]
```http
POST /api/auth/signin HTTP/1.1
Host: localhost:9000
Content-Type: application/json

{
  "username":"rmrhtdms",
  "password":"1234"
}
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkNTRMQUNDaThyRDlDc2hjelpLWU1TdTFHSXpqL1BkTVUyZ0kvbmhzcVhDWHhTNXB3RUNRNS4iLCJpYXQiOjE1NjYxMzE4OTQsImV4cCI6MTU2NjEzMjQ5NH0.sjOoNfSEifTAN2bQY3Ot6ds9aBPBKDd2YQOIEQVATIM",
  "issued_at": "Sun Aug 18 21:38:14 KST 2019",
  "expires_in": "Sun Aug 18 21:48:14 KST 2019"
}
```

### 토큰 갱신 (refresh) 
> refresh 토큰 재발급 API: 기존에 발급받은 토큰을 Authorization 헤더에 "Bearer Token"으로 입력 요청을 하면 토큰을 재발급한다.
#### [Request]
```http
PUT /api/auth/refresh HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkNTRMQUNDaThyRDlDc2hjelpLWU1TdTFHSXpqL1BkTVUyZ0kvbmhzcVhDWHhTNXB3RUNRNS4iLCJpYXQiOjE1NjYxMzE4OTQsImV4cCI6MTU2NjEzMjQ5NH0.sjOoNfSEifTAN2bQY3Ot6ds9aBPBKDd2YQOIEQVATIM
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkNTRMQUNDaThyRDlDc2hjelpLWU1TdTFHSXpqL1BkTVUyZ0kvbmhzcVhDWHhTNXB3RUNRNS4iLCJpYXQiOjE1NjYxMzIwMjksImV4cCI6MTU2NjEzMjYyOX0.pydzSiXHvHMfQlFXf59BZWipwV41_3fi_1uHevrkSmQ",
  "issued_at": "Sun Aug 18 21:40:29 KST 2019",
  "expires_in": "Sun Aug 18 21:50:29 KST 2019"
}
```

## 2. 주택금융 공급현황 분석 서비스

### 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API 개발
#### [Request]
```http
POST /api/finance/init HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkNTRMQUNDaThyRDlDc2hjelpLWU1TdTFHSXpqL1BkTVUyZ0kvbmhzcVhDWHhTNXB3RUNRNS4iLCJpYXQiOjE1NjYxMzMxMjcsImV4cCI6MTU2NjEzMzcyN30.Bz9vhzhnPiePmWJcyfV4hSvhWonDNEED7tJrXduDEiA
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "succeed"
}
```

### 주택금융 공급 금융기관(은행) 목록을 출력하는 API 를 개발하세요.
#### [Request]
```http
GET /api/finance/list HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYxMzMzMDMsImV4cCI6MTU2NjEzMzkwM30.JX4uAInBUzr0r7Sw8gNat_U6pcYuojfuo3axKy7yetA
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": 1,
    "bank": "주택도시기금"
  },
  ...
  {
    "id": 9,
    "bank": "기타은행"
  }
]
```

### 년도별 각 금융기관의 지원금액 합계를 출력하는 API 를 개발하세요.
#### [Request]
```http
GET /api/finance/summary-by-yearly HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYxMzM1MjcsImV4cCI6MTU2NjEzNDEyN30.96lR6PdCT-WArI-JUeG9ioKeOj-hVSO72u2A8SwU-2o
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "name": "주택금융 공급현황",
  "data": [
    {
      "year": "2005년",
      "total_amount": 48016,
      "detail_amount": {
        "농협은행/수협은행": 1486,
        "하나은행": 3122,
        "우리은행": 2303,
        "국민은행": 13231,
        "신한은행": 1815,
        "외환은행": 1732,
        "주택도시기금": 22247,
        "기타은행": 1376,
        "한국시티은행": 704
      }
    },
    ...
    {
      "year": "2017년",
      "total_amount": 295126,
      "detail_amount": {
        "농협은행/수협은행": 26969,
        "하나은행": 35629,
        "우리은행": 38846,
        "국민은행": 31480,
        "신한은행": 40729,
        "외환은행": 0,
        "주택도시기금": 85409,
        "기타은행": 36057,
        "한국시티은행": 7
      }
    }
  ]
}
```

### 각 년도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명을 출력하는 API 개발
#### [Request]
```http
GET /api/finance/maximum-by-yearly HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYxMzM2NzcsImV4cCI6MTU2NjEzNDI3N30.xQ2p40oe0OQtiA-q1t_fNJt7cgwCsopz20cW_BKSbrc
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "year": 2014,
  "bank": "주택도시기금"
}
```

### 전체 년도(2005 ~ 2016)에서 ~~외환은행~~ 특정은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API 개발
#### [Request]
```http
GET /api/finance/max-min-by-yearly HTTP/1.1
Host: localhost:9000
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYxMzM2NzcsImV4cCI6MTU2NjEzNDI3N30.xQ2p40oe0OQtiA-q1t_fNJt7cgwCsopz20cW_BKSbrc

{
	"bank":"외환은행"
}
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "bank": "외환은행",
  "support_amount": [
    {
      "year": 2015,
      "amount": 1702
    },
    {
      "year": 2017,
      "amount": 0
    }
  ]
}
```

### 특정 은행의 특정 달에 대해서 2018년도 해당 달에 금융지원 금액을 예측하는 API 개발
#### [Request]
```http
GET /api/finance/forecast HTTP/1.1
Host: localhost:9000
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYyODk1NDYsImV4cCI6MTU2NjI5MTM0Nn0.-Fld1qhJJXUuLNjK4jGPMe9DZicgGzIbZGwOW0oejZc

{
  "bank":"국민은행",
  "month": 2
}
```
#### [Response]
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "bank": 2,
  "year": 2018,
  "month": 2,
  "amount": 3700
}
```

## 3. 에러 메시지(Error Messages)
| Error code | Error Message                | HTTP Status Code          | Detailed Infomation                                  |
|------------|------------------------------|---------------------------|------------------------------------------------|
| -1         | INVALID_PARAMETER            | 400 Bad Request           | 잘못된 파라미터로 요청 시 발생                |
| -2         | USERNAME_DUPLICATE           | 400 Bad Request           | 중복된 username으로 가입 시도 시 발생         |
| -3         | PASSWORD_NOT_EQUALS          | 400 Bad Request           | 잘못된 password로 로그인 시도 시 발생         |
| -4         | ALREADY_REGISTERED_CSV_FILE  | 400 Bad Request           | CSV 파일 입력 중복 시도 시 발생                |
| -101       | AUTH_TOKEN_INVALID           | 401 Unauthorized          | 잘못된 JWT 사용 시 발생                       |
| -102       | AUTH_TOKEN_EXPIRED           | 401 Unauthorized          | 만료된  JWT 사용 시 발생                      |
| -103       | AUTH_TOKEN_INVALID_SIGNATURE | 401 Unauthorized          | 서명이 올바르지 않은 JWT 사용 시 발생          |
| -301       | USERNAME_NOT_FOUND           | 404 Not Found             | 존재하지 않는 username으로 로그인 시도 시 발생 |
| -302       | BANK_NOT_FOUND               | 404 Not Found             | 존재하지 않는 금융기관 이름을 입력 시 발생     |
| -401       | CSV_IO_EXCEPTION             | 500 Internal Server Error | CSV 입출력 과정에서 에러 발생 시               |

#### [Sample request]
```http
GET /api/finance/max-min-by-yearly HTTP/1.1
Host: localhost:9000
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkTDYxdU9ieTBIRkVkOVNCOWs1US5qdWVINjJkRWFhYlRzN0dLbXdyYS9IWmJBOG8yMWlHY0MiLCJpYXQiOjE1NjYyODk5ODMsImV4cCI6MTU2NjI5MTc4M30.sbhlUjiWTpYKGJUAIqO2DNa8nSol3RIfb1YTqloxthM

{
  "bank":"없는은행"
}
```
#### [Sample response]
```http
HTTP/1.1 404 Not Found
Date: Tue, 20 Aug 2019 08:33:16 GMT
Content-Type: application/json
Content-Length: 52

{
  "error_message": "BANK_NOT_FOUND",
  "error_code": -302
}
```

# 프로젝트 주요 패키지 설명 (Package description)
```bash
$ tree ./app -L 3 -C
app
├── ErrorHandler.java -----------------> 에러 발생 시, json 에러 메시지를 출력하기 위한 커스텀 에러 핸들러
├── Module.java -----------------------> 커스텀 DI 바인딩을 위한 모듈 클래스
├── actions
│   └── SecuredAction.java ------------> HTTP Request 헤더를 읽어 JWT를 검증하는 액션 클래스
├── controllers -----------------------> 컨트롤러
│   ├── FinanceController.java
│   ├── HomeController.java
│   └── UserController.java
├── exceptions
│   └── FinanceRuntimeException.java --> 예외 처리를 위한 Runtime Exception
├── models ----------------------------> 엔터티 모델
│   ├── Bank.java
│   ├── Finance.java
│   └── User.java
├── resources
│   ├── data.csv ----------------------> 문제에서 주어진 raw 데이터 파일
│   └── img-db-diagram.png ------------> DB 스키마를 확인할 수 있는 다이어그램 이미지
├── services --------------------------> 비즈니스 로직이 정의된 인터페이스
│   ├── BankService.java
│   ├── FinanceService.java
│   ├── UserService.java
│   └── impl --------------------------> 비즈니스 로직을 구현한 클래스
│       ├── BankServiceImpl.java
│       ├── FinanceServiceImpl.java
│       └── UserServiceImpl.java
└── views
    ├── index.scala.html
    └── main.scala.html
```

# 사용한 주요 오픈소스 목록 (List of used open source libraries)
```bash
# CSV 파일을 다루기 위한 라이브러리
libraryDependencies += "com.opencsv"% "opencsv"% "4.1"

# JWT를 Java로 구현한 라이브러리
libraryDependencies += "io.jsonwebtoken" % "jjwt-api" % "0.10.7"
libraryDependencies += "io.jsonwebtoken" % "jjwt-impl" % "0.10.7"
libraryDependencies += "io.jsonwebtoken" % "jjwt-jackson" % "0.10.7"

# 지원금액 예측을 위한 시계열(time series) 라이브러리
libraryDependencies += "com.github.signaflo" % "timeseries" % "0.4"

# 사용자 암호 인코딩을 위한 bcrypt 라이브러리
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
```

# 데이터베이스 다이어그램 (Database diagram)
![No image](https://raw.githubusercontent.com/DaegiKim/play-housing-finance-app/master/app/resources/img-db-diagram.png)

[End Of File]