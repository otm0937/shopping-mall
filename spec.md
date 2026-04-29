# 쇼핑몰 프로젝트 명세서

## 프로젝트 개요

Spring Framework와 CRUD 로직을 기반으로 한 간단한 쇼핑몰 구현 프로젝트입니다. 
상품 등록, 회원가입, 결제 등의 커머스 핵심 기능을 포함합니다.

### 습득 역량
- Spring 기반의 CRUD 기능 구현
- 쇼핑몰 데이터 모델링
- 인증 처리 로직 구현
- MVC 패턴의 이해와 활용

### 결과물
- 웹 애플리케이션 기획 문서
- DB 스키마 및 기능 구현 명세
- 쇼핑몰 결과물 배포 URL

## 요구사항

### 아키텍처
- 쇼핑몰 웹, 관리자 웹 페이지, 백엔드 API 3가지를 모노레포 구조로 구현
- 프론트엔드: Next.js
- 백엔드: Spring Boot

### 기능
- [x] 상품 등록, 조회, 수정, 삭제 (CRUD)
- [x] 회원가입, 로그인 (JWT 인증)
- [x] 상품 장바구니 기능
- [x] 결제 기능 (Mock 결제 - 실제 결제 X)
- [x] 결제 내역 조회
- [x] 관리자/사용자 권한 분리

### 기술적 요구사항
- [x] API 문서: Swagger
- [x] 데이터베이스: MariaDB
- [x] 컨테이너화: Docker & Docker Compose
- [x] 상품 이미지 업로드 지원
- [x] 반응형 웹 디자인
- [x] Vapor Design System 적용

### 배포
- [x] Docker Compose로 통합 배포
- [x] EC2 배포 가이드 제공

## 프로젝트 구조

```
shopping-mall/
├── backend/          # Spring Boot (Kotlin)
├── frontend/         # Next.js (TypeScript)
├── docker-compose.yml
└── README.md
```

## 실행 방법

```bash
# Docker Compose로 실행
docker-compose up --build -d

# 접속
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
```

## 테스트 계정

- 관리자: root / root

## 라이선스

MIT License
