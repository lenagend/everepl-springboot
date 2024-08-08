# 에브리플 프로젝트

작업 영역: 웹 애플리케이션 제작
스택: aws, docker, java, react.js
작업기간: 2023년 12월 29일 → 2024년 6월 25일
URL: [https://everepl.com](https://everepl.com/)
기여도: 1

### **프로젝트 개요**

- **프로젝트 이름:** 에브리플(everepl)
- **프로젝트 설명:** 에브리플은 사용자가 다양한 주제에 대해 토론하고 소통할 수 있는 웹 커뮤니티 플랫폼입니다. 사용자는 url을 입력해 페이지정도가 추출되면 댓글을 달 수 있습니다. 댓글이 중지된 서비스(유튜브, 네이버뉴스 등)에 댓글을 달 수 있습니다. 이 프로젝트는 스프링부트와 리액트를 활용하여 구축되었으며, 도커화 되었으며, AWS EC2를 통해 배포되어 운영 중입니다.
    - 최근 JVM 힙 메모리 문제를 해결하기 위해 자바로 수행하던 일부 기능을 파이썬으로 재구현하여 성능과 안정성을 개선하였습니다.

### 기술 스택

### 백엔드

- **프레임워크:** Spring Boot (v3.2.1)
- **언어:** Java (v21), python(3.12)
- **데이터베이스:** H2, MariaDB
- **ORM:** Spring Data JPA
- **보안:** Spring Security, JWT, OAuth2 (구글, 카카오, 네이버)
- **웹소켓:** Spring WebSocket (실시간 알림에 사용)
- **API:** RESTful API
- **빌드 툴:** Maven
- **테스팅:** Spring Boot Starter Test
- **클라우드:** AWS EC2, AWS S3
- **기타:** Jsoup, QueryDSL, Selenium
- **헬스 체크:** Spring Boot Actuator

### 프론트엔드

- **프레임워크:** React (v18.2.0)
- **상태 관리:** React Context API
- **라우팅:** React Router Dom (v6.18.0)
- **스타일링:** MUI (Material-UI), Emotion
- **HTTP 클라이언트:** Axios
- **텍스트 에디터:** React Quill
- **테스팅:** React Testing Library
- **빌드 툴:** React Scripts
- **기타:** Moment, Date-fns, SockJS

### 주요 기능

- **사용자 인증 및 권한 관리:** JWT 및 OAuth2(구글, 카카오, 네이버)를 사용한 인증 시스템.

![untitle](https://github.com/user-attachments/assets/64b66e2d-8ff9-48c6-81e7-d600541d92dc)

![untitle](https://github.com/user-attachments/assets/20888f0f-8a20-4c97-83d4-01b3e3b7b094)

- **게시판 기능:** CRUD (Create, Read, Update, Delete) 기능을 제공하는 게시판.

![untitle](https://github.com/user-attachments/assets/093fbd2f-6a1a-4e30-ae3d-7ec023d784d8)

![untitle](https://github.com/user-attachments/assets/3eca1c31-544e-46a3-bcf4-fea2f419186f)

- **실시간 알림:** WebSocket을 통한 실시간 알림 기능.

![untitle](https://github.com/user-attachments/assets/2052e16d-4000-495f-89f1-4245d3888542)

![untitle](https://github.com/user-attachments/assets/02285aee-d1b1-4481-b992-faa8c9e7da37)

- **파일 업로드:** AWS S3를 통한 파일 업로드 및 관리.

![untitle](https://github.com/user-attachments/assets/65eb9bbe-bdeb-4b14-8307-30158296b767)

![untitle](https://github.com/user-attachments/assets/9556d10c-cafe-45f1-b704-aa6f82a974ba)

- **검색 및 필터:** 다양한 검색 및 필터 기능.

![untitle](https://github.com/user-attachments/assets/67c5b5f9-dbfe-4d0c-a758-d54da38e023f)

![untitle](https://github.com/user-attachments/assets/96dfcae6-5e84-4ed1-912f-51b5fc814eed)

![untitle](https://github.com/user-attachments/assets/0c3b3f13-2aa0-4244-8e44-2de56beedcd7)

- **모바일 대응:** 반응형 웹 디자인을 통한 다양한 디바이스 지원.

![untitle](https://github.com/user-attachments/assets/9f64b0f5-c1d4-4a83-a12d-e420120e05fd)

![untitle](https://github.com/user-attachments/assets/62f109d0-0cf1-4288-8c88-b297f081ff08)

- **HTTPS 리다이렉션:** AWS 로드 밸런서를 이용하여 HTTPS 인증서 적용 및 자동 리다이렉션.

![untitle](https://github.com/user-attachments/assets/3f2b7ebe-1c8a-4d24-baac-677a65335bb0)

- **주기적인 헬스 체크:** Spring Boot Actuator와 연동하여 주기적인 헬스 체크 수행.

![untitle](https://github.com/user-attachments/assets/68c01084-ed24-48b9-8243-9f33e724adaf)

### 프로젝트 아키텍처

- **백엔드 구조:** RESTful API를 제공하며, Spring Boot를 통해 비즈니스 로직을 처리합니다.
- **프론트엔드 구조:** React를 통해 사용자 인터페이스를 구축하였으며, 컴포넌트 기반으로 설계되었습니다.
- **데이터베이스:** H2 데이터베이스를 로컬 개발 환경에서 사용하고, 프로덕션 환경에서는 MariaDB를 사용합니다.
- **배포:** AWS EC2 인스턴스를 활용하여 애플리케이션을 배포하였으며, AWS 로드 밸런서를 통해 HTTPS 인증서를 적용하고 자동 리다이렉션을 설정하였습니다.
- **헬스 체크:** Spring Boot Actuator를 통해 주기적인 헬스 체크를 수행하여 애플리케이션의 상태를 모니터링합니다.
- **도커화 및 도커 컴포즈:** 각 서버를 도커화하고, Docker Compose를 사용하여 쉽게 배포 및 관리를 할 수 있도록 구성하였습니다.

### 주요 기능 업데이트

- **데이터 수집 및 처리:** 자바로 수행하던 데이터 수집 및 처리 작업을 파이썬으로 재구현하여 메모리 사용량을 최적화하고 성능을 개선했습니다. 구체적으로, Google 트렌드에서 데이터를 수집하여 MariaDB에 저장하는 작업을 파이썬으로 처리하였습니다.

### 프로젝트 URL

- **웹사이트:** [https://everepl.com](https://everepl.com/)

### GitHub Repository

- **GitHub:**
1. 스프링부트 레포지토리: https://github.com/lenagend/everepl-springboot
2. 리액트 레포지토리: https://github.com/lenagend/everepl-react

### 개인 프로젝트: 배운 점과 부족한 점

이 프로젝트는 100% 개인 프로젝트로, 프론트엔드, 백엔드, 배포 서버까지 모두 혼자서 개발하고 관리했습니다.

- **배운 점**
    - **종합적인 개발 경험:** 프론트엔드와 백엔드, 클라우드 배포까지 전 과정을 경험하면서 종합적인 웹 개발 능력을 키웠습니다.
    - **문제 해결 능력:** 혼자서 모든 문제를 해결해야 했기 때문에, 문제 해결 능력이 크게 향상되었습니다.
    - **자기 주도 학습:** 필요한 기술을 빠르게 학습하고 적용하는 자기 주도 학습 능력이 강화되었습니다.
    - **책임감:** 모든 부분을 혼자 담당하면서 프로젝트의 모든 면에 책임감을 가지게 되었습니다.
- **부족한 점**
    - **협업 경험 부족:** 혼자서 모든 작업을 진행하다 보니 팀 협업 경험이 부족합니다. 다른 개발자와의 협업, 코드 리뷰, 협업 도구 활용 능력이 필요합니다.
    - **전문성 부족:** 여러 부분을 혼자서 처리하다 보니, 특정 분야에 대한 깊이 있는 전문성이 부족할 수 있습니다. 예를 들어, 보안이나 데이터베이스 최적화와 같은 부분입니다.
    - **시간 관리:** 모든 것을 혼자서 하다 보니 시간 관리가 어려웠습니다. 프로젝트 일정 관리와 우선순위 설정에 대한 개선이 필요합니다.