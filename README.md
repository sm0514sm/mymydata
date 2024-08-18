# 마이데이터 에이전트 프로젝트
[mymydata.webm](https://github.com/user-attachments/assets/d61188c4-bed2-4d35-8866-c890db8acd4f)



## 프로젝트 개요

이 프로젝트는 마이데이터 공식 문서를 기반으로 정책과 기술 사양 등을 답변해주는 AI 에이전트를 구현한 것입니다. Spring Boot와 Spring AI를 기반으로 구축되었으며, RAG(Retrieval-Augmented Generation) 기술을 활용하여 사용자 질문에 정확하고 관련성 있는 답변을 제공합니다.

## 주요 구현 기능

- 주어진 리소스 기반으로 마이데이터 관련 질문에 대한 AI 기반 응답
- 채팅 기억 관리자를 통한 멀티턴 에이전트 구현
- 멀티모달 지원
- 실시간 채팅 기능
- 다중 채널 지원
- 요청 토큰 수 초과 및 500 에러 발생시 재시도 지원

## 기술 스택

- Java 21
- Spring Boot
- Spring AI
- Vaadin (웹 프레임워크)
- OpenAI GPT 모델
- Chroma Vector Store

## 설치 및 실행 방법

1. 저장소 클론:
   ```
   git clone https://github.com/sm0514sm/mymydata.git
   ```

2. 프로젝트 디렉토리로 이동:
   ```
   cd mymydata
   ```

3. 애플리케이션 빌드:
   ```
   ./gradlew build
   ```

4. (Option) Chroma VectorStore docker 컨테이너 실행
   ```
   docker run -it --rm --name chroma -p 8000:8000 ghcr.io/chroma-core/chroma:0.4.15
   ```
5. 애플리케이션 실행:
   ```
   ./gradlew bootRun
   ```

6. 웹 브라우저에서 `http://localhost:8080` 접속

## 설정

`application.yml` 파일에서 다음 설정을 변경할 수 있습니다:

- OpenAI API 키: `spring.ai.openai.api-key`
- 사용할 OpenAI LLM 모델: `spring.ai.openai.chat.options.model`
- 응답 temperature: `spring.ai.openai.chat.options.temperature`
  - 응답의 정확함을 위해 기본값보다 낮은 0.2로 설정하였습니다. 필요 시 값을 수정해주세요.
- 사용할 OpenAI embedding 모델: `spring.ai.openai.embedding.options.model`
- OpenAI가 아닌 다른 모델을 사용할 경우, 적절한 자동구성 dependency를 추가해야합니다. 
  - https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- 벡터 스토어 설정: `app.vectorstore.target`
  - 선택 가능한 벡터 스토어: simple, chroma (chroma 지정 시 초기 데이터베이스 설정에 1시간 이상 소요될 수 있습니다.)
  - simple vectorstore는 프로젝트 최상단에 simple-vectorstore.json으로 저장되어있습니다.
- 마이데이터 문서 URL: `app.resources`

## 사용 방법

1. 웹 인터페이스에 접속합니다.
2. 채널을 선택하거나 새로운 채널을 생성합니다.
3. 채팅 인터페이스에서 마이데이터 관련 질문을 입력합니다.
4. AI 에이전트의 응답을 확인합니다.

## 주요 컴포넌트

- `ChatService`: 사용자 메시지 처리 및 AI 응답 생성
- `ChannelService`: 채널 관리
- `MessageService`: 메시지 저장 및 검색
- `VectorStoreConfig`: 벡터 스토어 설정 및 문서 처리
- `LobbyView` & `ChannelView`: 웹 인터페이스 컴포넌트
