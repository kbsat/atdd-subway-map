package wooteco.subway.ui;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static wooteco.subway.test.TestFixture.봉천역;
import static wooteco.subway.test.TestFixture.사당역;
import static wooteco.subway.test.TestFixture.서울대입구역;
import static wooteco.subway.test.TestFixture.신림역;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.LineRequest;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.SectionRequest;
import wooteco.subway.service.LineService;

@WebMvcTest(LineController.class)
public class LineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LineService lineService;

    @BeforeEach
    void init() {

    }

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() throws Exception {
        // given
        LineRequest test = new LineRequest("신림역", "GREEN", 1L, 2L, 5);
        given(lineService.save(any(LineRequest.class)))
                .willReturn(LineResponse.of(new Line(1L, "신림역", "GREEN"),
                        List.of(new Station(1L, "신림역"), new Station(2L, "봉천역"))));
        // when₩
        ResultActions perform = mockMvc.perform(post("/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test)));
        // then
        perform.andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").value("신림역"))
                .andExpect(jsonPath("color").value("GREEN"))
                .andExpect(jsonPath("stations[0].id").value(1L))
                .andExpect(jsonPath("stations[0].name").value("신림역"))
                .andExpect(jsonPath("stations[1].id").value(2L))
                .andExpect(jsonPath("stations[1].name").value("봉천역"))
                .andExpect(header().stringValues("Location", "/lines/1"));
    }


    @DisplayName("지하철 노선 생성 시 이름이 중복된다면 에러를 응답한다.")
    @Test
    void createLine_duplication_exception() throws Exception {
        // given
        LineRequest test = new LineRequest("2호선", "GREEN", 1L, 2L, 5);
        given(lineService.save(any(LineRequest.class)))
                .willThrow(new IllegalArgumentException("2호선 : 이름이 중복되는 지하철 노선이 존재합니다."));
        // when
        ResultActions perform = mockMvc.perform(post("/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test)));
        // then
        perform.andExpectAll(
                status().isBadRequest(),
                jsonPath("message").value("2호선 : 이름이 중복되는 지하철 노선이 존재합니다.")
        );
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() throws Exception {
        // given
        given(lineService.findAll())
                .willReturn(List.of(
                        LineResponse.of(new Line(1L, "test1", "GREEN"), List.of(신림역, 봉천역, 서울대입구역)),
                        LineResponse.of(new Line(2L, "test2", "YELLOW"), List.of(사당역, 신림역))));
        // when
        ResultActions perform = mockMvc.perform(get("/lines"));
        // then
        perform.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.length()").value(2),
                jsonPath("$[0].id").value(1),
                jsonPath("$[0].name").value("test1"),
                jsonPath("$[0].color").value("GREEN"),
                jsonPath("$[0].stations[0].name").value("신림역"),
                jsonPath("$[0].stations[1].name").value("봉천역"),
                jsonPath("$[0].stations[2].name").value("서울대입구역"),
                jsonPath("$[1].id").value(2),
                jsonPath("$[1].name").value("test2"),
                jsonPath("$[1].color").value("YELLOW"),
                jsonPath("$[1].stations[0].name").value("사당역"),
                jsonPath("$[1].stations[1].name").value("신림역")
        );
    }

    @DisplayName("id를 이용해 지하철 노선을 조회한다.")
    @Test
    void getLine() throws Exception {
        // given
        given(lineService.findById(1L))
                .willReturn(LineResponse.of(new Line(1L, "test1", "GREEN"),
                        List.of(신림역, 봉천역, 서울대입구역)));
        // when
        ResultActions perform = mockMvc.perform(get("/lines/1"));
        // then
        perform.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("id").value(1),
                jsonPath("name").value("test1"),
                jsonPath("color").value("GREEN"),
                jsonPath("stations[0].name").value("신림역"),
                jsonPath("stations[1].name").value("봉천역"),
                jsonPath("stations[2].name").value("서울대입구역")
        );
    }

    @DisplayName("존재하지 않는 id를 이용해 지하철 노선을 조회할 경우 에러가 발생한다.")
    @Test
    void getLine_noExistLine_exception() throws Exception {
        // given
        given(lineService.findById(1L))
                .willThrow(new IllegalArgumentException("1 : 해당 ID의 지하철 노선이 존재하지 않습니다."));
        // when
        ResultActions perform = mockMvc.perform(get("/lines/1"));
        // then
        perform.andExpectAll(
                status().isBadRequest(),
                jsonPath("message").value("1 : 해당 ID의 지하철 노선이 존재하지 않습니다.")
        );
    }

    @DisplayName("지하철 노선을 제거한다.")
    @Test
    void deleteLine() throws Exception {
        // given
        given(lineService.findById(1L))
                .willReturn(LineResponse.of(new Line(1L, "test", "BLACK")));
        // when
        ResultActions perform = mockMvc.perform(delete("/lines/1"));
        // then
        perform.andExpect(status().isNoContent());
    }

    @DisplayName("삭제 요청 시 ID에 해당하는 지하철 노선이 없다면 에러를 응답한다.")
    @Test
    void deleteLine_noExistLine_exception() throws Exception {
        // given
        doThrow(new IllegalArgumentException("1 : 해당 ID의 지하철 노선이 존재하지 않습니다."))
                .when(lineService)
                .delete(1L);
        // when
        ResultActions perform = mockMvc.perform(delete("/lines/1"));
        // then
        perform.andExpectAll(
                status().isBadRequest(),
                jsonPath("message").value("1 : 해당 ID의 지하철 노선이 존재하지 않습니다.")
        );
    }

    @DisplayName("노선을 수정한다.")
    @Test
    void updateLine() throws Exception {
        // given
        LineRequest updateRequest = new LineRequest("2호선", "GREEN", 1L, 2L, 5);
        // when
        ResultActions perform = mockMvc.perform(put("/lines/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));
        // then
        assertAll(
                () -> perform.andExpect(status().isOk()),
                () -> verify(lineService).update(anyLong(), any(LineRequest.class))
        );
    }

    @DisplayName("존재하지 않는 ID의 노선을 수정한다.")
    @Test
    void updateLine_noExistLine_Exception() throws Exception {
        // given
        LineRequest updateRequest = new LineRequest("2호선", "GREEN", 1L, 2L, 5);
        doThrow(new IllegalArgumentException("1 : 해당 ID의 지하철 노선이 존재하지 않습니다."))
                .when(lineService)
                .update(anyLong(), any(LineRequest.class));
        // when
        ResultActions perform = mockMvc.perform(put("/lines/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));
        // then
        perform.andExpectAll(
                status().isBadRequest(),
                jsonPath("message").value("1 : 해당 ID의 지하철 노선이 존재하지 않습니다.")
        );
    }

    @DisplayName("중복된 이름으로 노선을 수정한다.")
    @Test
    void updateLine_duplicateName_Exception() throws Exception {
        // given
        LineRequest updateRequest = new LineRequest("2호선", "GREEN", 1L, 2L, 5);
        doThrow(new IllegalArgumentException("2호선 : 이름이 중복되는 지하철 노선이 존재합니다."))
                .when(lineService)
                .update(anyLong(), any(LineRequest.class));
        // when
        ResultActions perform = mockMvc.perform(put("/lines/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));
        // then
        perform.andExpectAll(
                status().isBadRequest(),
                jsonPath("message").value("2호선 : 이름이 중복되는 지하철 노선이 존재합니다.")
        );
    }

    @DisplayName("노선에 구간을 추가한다.")
    @Test
    void addSection() throws Exception {
        // given
        SectionRequest sectionRequest = new SectionRequest(1L, 2L, 10);
        // when
        ResultActions perform = mockMvc.perform(post("/lines/1/sections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sectionRequest)));
        // then
        assertAll(
                () -> perform.andExpect(status().isOk()),
                () -> verify(lineService).addSection(anyLong(), any(SectionRequest.class))
        );
    }

    @DisplayName("노선에서 구간을 제거한다.")
    @Test
    void deleteSection() throws Exception {
        // when
        ResultActions perform = mockMvc.perform(delete("/lines/1/sections?stationId=2"));
        // then
        assertAll(
                () -> perform.andExpect(status().isOk()),
                () -> verify(lineService).deleteSection(anyLong(), anyLong())
        );
    }
}
