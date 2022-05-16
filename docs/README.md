
## 지하철 노선도 미션
### 1단계 요구사항 정리
- [x] 지하철 역 관리 API 기능 완성하기
  - [x] 지하철역 등록 `POST /stations`
    - `요청` json으로 name을 전송한다. 
    - 결과 상태 코드는 `201 Created` 이다.
    - `응답` json으로 id, name을 반환한다.
    - Location 헤더는 `/stations/{id}` 이다.
    - `예외` 지하철 역 생성 시 이름이 중복된다면 에러를 응답한다.
  - [x] 지하철역 목록 조회 `GET /stations`
    - 결과 상태 코드는 `200 OK` 이다.
    - `응답` json으로 id, name을 가진 리스트를 반환한다.
  - [x] 지하철역 삭제 `DELETE /stations/{id}`
    - 결과 상태 코드는 `204 No Content` 이다.
- [x] 지하철 노선 관리 API 기능 완성하기
  - [x] 노선 등록 `POST /lines`
    - `요청` json으로 name, color를 전송한다.
    - 결과 상태 코드는 `201 Created` 이다.
    - Location 헤더는 `/lines/{id}` 이다.
    - `응답` json으로 id, name, color를 반환한다.
    - `예외` 지하철 노선 생성 시 이름이 중복된다면 에러를 응답한다.
  - [x] 노선 목록 조회 `GET /lines`
    - 결과 상태 코드는 `200 OK` 이다.
    - `응답` json으로 id, name, color를 가진 리스트를 반환한다.
  - [x] 노선 조회 `GET /lines/{id}`
    - 결과 상태 코드는 `200 OK` 이다.
    - `응답` json으로 id, name, color를 반환한다.
  - [x] 노선 수정 `PUT /lines/{id}`
    - `요청` json으로 name, color를 전송한다.
    - 결과 상태 코드는 `200 OK` 이다.
  - [x] 노선 삭제 `DELETE /lines/{id}`
    - 결과 상태 코드는 `204 No Content` 이다.

### 2단계 요구사항 정리
- [x] 스프링 JDBC 활용하여 H2 DB에 저장하기
  - Dao 객체가 아닌 DB에서 데이터 관리
  - DB에 접근하기 위한 Spring JDBC 라이브러리 활용
- [x] H2 DB를 통해 저장된 값 확인하기
  - 실제 DB에 저장이 잘 되었는지 확인할 수 있도록 설정하기
  - h2 console 활용 가능
- [x] 스프링 빈 활용하기

### 3단계 요구사항 정리
- [x] 서비스 레이어 추가하기
1. 도메인에 요구사항 적용
   - [x] Station 도메인
     - Station 도메인의 역할 : 역에 대한 표현을 한다.
     - 필드 : id, name
       - 이름이 null 혹은 빈 문자열이면 안된다.
       - 이름이 255자를 초과하면 안된다.
     - name이 같은지 확인해주는 책임을 가진다.
   - [x] Section 도메인
     - Section 도메인의 역할 : 역과 역을 잇는 구간에 대한 표현을 한다.
     - 필드 : upStation, downStation, distance
       - `예외` 거리는 0 또는 음수이면 안된다.
       - `예외` upStation, downStation은 null이면 안된다.
   - [x] Sections 도메인
     - Sections 도메인의 역할 : 하나의 노선에 존재하는 구간들을 표현해준다.
     - 필드 : List<Sectoin> sections
       - sections는 적어도 하나의 Section을 가지고 있어야 한다.
     - [x] 새로운 구간을 추가한다.
       - `예외` 상행역, 하행역 둘 중 하나도 노선에 포함되지 않는 경우 예외를 발생시킨다.
       - `예외` 기존에 노선에 해당 상행역, 하행역이 이미 등록되어 있다면 구간을 등록할 수 없다.
       - 새로운 구간이 기존에 존재하는 구간 맨 앞 혹은 맨 뒤에 삽입하는 경우 ( 그냥 add )
       - 새로운 구간이 기존에 존재하는 구간 사이에 삽입하는 경우 ( A-B =(A-C추가)=> A-C-B )
         - `예외` 구간 사이에 추가할 경우 기존 역 사이의 길이보다 크거나 같으면 예외를 발생시킨다.
         - 삽입 한 뒤에 `C-B 간의 거리`는 `A-B 간의 거리 - A-C간의 거리`가 된다.
         - A - B 에 A-C 를 추가하는 것 뿐만아니라 C-B 를 추가하는 것도 가능해야한다.
     - [x] 구간을 제거한다.
       - Station을 입력받아 해당 Station이 포함된 구간을 제거한다.
         - `예외` 제거하려는 Station이 포함된 Section이 존재하지 않는 경우 예외를 발생시킨다.
         - `예외` sections의 사이즈가 1일 때 제거하려고 하면 예외를 발생시킨다.
         - 맨 앞 혹은 맨 뒤 구간을 제거하는 경우
           - 그냥 삭제만 하면 된다.
         - 구간 사이의 역을 제거하는 경우
           - 두 구간이 영향을 받는다.
           - 만일 A-B-C 에서 B 역을 제거할 경우 B가 포함된 `A-B`, `B-C`를 제거하고 `A-C` 구간을 새로 생성한다.
             - 이 때, 거리는 `A-B 구간의 거리 + B-C 구간의 거리`
2. 지하철 노선 추가 API 수정 `POST /lines`
   - [x] 노선 추가 시 구간 정보를 함께 등록한다.
     - 결과 상태 코드는 `200 OK` 이다.
     - `요청` json으로 name, color, upStationId, downStationId, distance를 받는다.
     - `응답` json으로 id, name, color, stations(station 리스트)를 반환한다.
     - stations의 요소는 station의 id, name을 각각 가진다.
   - [x] 노선 조회 시 노선에 해당하는 역 목록도 추가로 조회한다. `GET /lines/{id}`
     - 결과 상태 코드는 `200 OK` 이다.
     - 역 목록은 상행선부터 하행선으로 정렬된 상태여야한다.
     - `응답` id, name, color, stations(station의 리스트)를 반환한다.
   - [x] 노선 목록 조회 시 노선에 해당하는 역 목록도 추가로 조회한다. `GET /lines`
     - 결과 상태 코드는 `200 OK` 이다.
     - `응답` id, name, color, stations를 요소로 가지는 리스트를 반환한다.
3. 구간 관리 API 구현
    - [x] 구간 추가 기능 노선에 구간을 추가 (`POST /lines/{line_id}/sections`)
      - 결과 상태 코드는 `200 OK` 이다.
      - `요청` json으로 upStationId, downStationId, distance를 받는다.
      - `응답` body 내용은 존재하지 않는다.
      - 추가하려는 upStationId, downStationId 둘 중 하나는 기존 노선에 포함되어있어야한다.
      - 기존에 존재하는 구간의 스테이션을 upStationId로 추가할 경우 기존 구간을 갈래길이 안생기도록 변경한다.
        - 새로 삽입하려는 구간의 길이가 기존에 존재하던 구간의 길이보다 짧아야한다.
        - 구간을 삽입할 경우 기존의 구간은 둘로 쪼개진다. (A-C =(A-B 추가)=> A-B, B-C)
        - 거리도 새로 할당해주어야한다.
      - `예외` 역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 등록할 수 없다.
      - `예외` 상행역, 하행역 둘 중 하나도 노선에 포함되있지 않으면 추가할 수 없다.
    - [x] 구간을 제거한다 (`DELETE /lines/{line_id}/sections?stationId={station_id})
      - 결과 상태 코드는 `200 OK` 이다. 
      - 구간에서 해당 station이 포함된 구간을 제거한다.
      - 종점이 제거될 경우 다음으로 오던 역이 종점이 된다.
      - 중간역이 제거되면 남아있는 두 구간은 이어지고 거리는 남아있는 두 구간 거리의 합으로 정해진다.
      - `예외`구간이 하나인 노선에서는 구간을 제거할 수 없다.


### 피드백 수정
- [x] 공백을 좀 더 활용하도록 수정
- [x] SectionService로 분리해보자
- [ ] Sections를 분리할 수 있을까? (구간 등록, 삭제 등..)
