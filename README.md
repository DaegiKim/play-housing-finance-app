# play-housing-finance-app
#### 주택 금융 서비스 API 개발

- 개발환경
    - Java 1.8 +
    - Play Framework(2.7.3, Java)
    - H2 Database (In-Memory) 
    - sbt

---
# API 명세서

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
#### [Error]
> WIP

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
#### [Error]
> WIP

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
#### [Error]
> WIP

## 2. 주택금융 공급현황 분석 서비스

### 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API 개발
#### [Request]
```http
POST /api/init HTTP/1.1
Host: localhost:9000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InJtcmh0ZG1zIiwicGFzc3dvcmQiOiIkMmEkMTAkNTRMQUNDaThyRDlDc2hjelpLWU1TdTFHSXpqL1BkTVUyZ0kvbmhzcVhDWHhTNXB3RUNRNS4iLCJpYXQiOjE1NjYxMzMxMjcsImV4cCI6MTU2NjEzMzcyN30.Bz9vhzhnPiePmWJcyfV4hSvhWonDNEED7tJrXduDEiA
```
#### [Response]
```http
HTTP/1.1 200 OK
```
#### [Error]
> WIP

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
  {
    "id": 2,
    "bank": "국민은행"
  },
  ...
  {
    "id": 9,
    "bank": "기타은행"
  }
]
```
#### [Error]
> WIP

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
#### [Error]
> WIP

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
#### [Error]
> WIP

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
#### [Error]
> WIP

### 특정 은행의 특정 달에 대해서 2018년도 해당 달에 금융지원 금액을 예측하는 API 개발
#### [Request]
#### [Response]
#### [Error]
> WIP

# WIP ...