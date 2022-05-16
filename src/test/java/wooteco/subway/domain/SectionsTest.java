package wooteco.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static wooteco.subway.test.TestFixture.낙성대역;
import static wooteco.subway.test.TestFixture.봉천역;
import static wooteco.subway.test.TestFixture.사당역;
import static wooteco.subway.test.TestFixture.서울대입구역;
import static wooteco.subway.test.TestFixture.신림역;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SectionsTest {

    @DisplayName("구간은 하나 이상 존재해야 합니다.")
    @Test
    void validateSize() {
        assertThatThrownBy(() -> new Sections(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("구간은 하나 이상 존재해야 합니다.");
    }

    @DisplayName("상행역, 하행역 둘 중 하나도 노선에 포함되지 않는 경우 예외를 발생시킨다.")
    @Test
    void insert_noStationException() {
        Sections sections = new Sections(new Section(신림역, 봉천역, 5));

        assertThatThrownBy(() -> sections.add(new Section(서울대입구역, 낙성대역, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상행역 또는 하행역이 노선에 포함되어 있어야합니다.");
    }

    @DisplayName("기존에 노선에 해당 상행역, 하행역이 이미 등록되어 있다면 구간을 등록할 수 없다.")
    @Test
    void insert_sameStationsException() {
        Sections sections = new Sections(new Section(신림역, 봉천역, 5));

        assertThatThrownBy(() -> sections.add(new Section(신림역, 봉천역, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 구간은 기존 노선에 이미 등록되어있습니다.");
    }

    @DisplayName("새로운 구간이 기존에 존재하는 구간 맨 뒤 혹은 맨 앞에 삽입하는 경우")
    @Test
    void insert_frontOrBack() {
        // given
        Section section = new Section(신림역, 봉천역, 5);
        Section newSection = new Section(서울대입구역, 신림역, 5);
        Sections sections = new Sections(section);
        // when
        sections.add(newSection);
        // then
        List<Section> results = sections.getSections();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).contains(section, newSection);
    }

    @DisplayName("새로운 구간이 기존에 존재하는 구간 중간에 삽입하는 경우 : upStation 일치")
    @Test
    void insertMiddle_upStation() {
        // given
        Section section = new Section(신림역, 봉천역, 5);
        Section section2 = new Section(봉천역, 낙성대역, 7);
        Section newSection = new Section(봉천역, 서울대입구역, 3);

        Sections sections = new Sections(List.of(section, section2));
        // when
        sections.add(newSection);
        // then
        List<Section> results = sections.getSections();
        assertThat(results.size()).isEqualTo(3);
        assertThat(results).contains(
                section,
                new Section(봉천역, 서울대입구역, 3),
                new Section(서울대입구역, 낙성대역, 4));
    }

    @DisplayName("새로운 구간이 기존에 존재하는 구간 중간에 삽입하는 경우 - downStation 일치")
    @Test
    void insertMiddle_downStation() {
        // given
        Section section = new Section(신림역, 봉천역, 5);
        Section newSection = new Section(낙성대역, 봉천역, 3);
        Sections sections = new Sections(section);
        // when
        sections.add(newSection);
        // then
        List<Section> results = sections.getSections();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).contains(
                new Section(신림역, 낙성대역, 2),
                newSection);
    }

    // a - b - c
    @DisplayName("새로운 구간이 기존에 존재하는 구간 중간에 삽입하는 경우 - 여러개일 경우")
    @Test
    void insertMiddle_compose() {
        // given
        Section section = new Section(신림역, 봉천역, 5);
        Section section2 = new Section(봉천역, 낙성대역, 5);
        Sections sections = new Sections(List.of(section, section2));
        // when
        Section newSection = new Section(봉천역, 서울대입구역, 3);
        sections.add(newSection);
        // then
        List<Section> results = sections.getSections();
        assertThat(results).contains(
                section,
                newSection,
                new Section(서울대입구역, 낙성대역, 2)
        );
    }

    @DisplayName("역 사이에 구간을 등록할 경우 기존 역 구간 길이보다 짧아야한다.")
    @Test
    void insertMiddle_distanceException() {
        // given
        Section section = new Section(봉천역, 낙성대역, 5);
        Section newSection = new Section(봉천역, 서울대입구역, 7);
        Sections sections = new Sections(section);
        // then
        assertThatThrownBy(() -> sections.add(newSection))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("역 사이에 구간을 등록할 경우 기존 역 구간 길이보다 짧아야 합니다.");
    }

    @DisplayName("제거하려는 Station이 포함된 Section이 존재하지 않는 경우 예외를 발생시킨다.")
    @Test
    void delete_noStationException() {
        // given
        Section section = new Section(봉천역, 낙성대역, 5);
        Section section2 = new Section(낙성대역, 신림역, 5);
        Sections sections = new Sections(section);
        sections.add(section2);
        // then
        assertThatThrownBy(() -> sections.delete(서울대입구역))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지우려는 역이 노선에 포함되어 있어야합니다.");
    }

    @DisplayName("제거하려는 구간의 사이즈가 1이면 예외를 발생시킨다.")
    @Test
    void delete_onlyOneSection() {
        // given
        Section section = new Section(봉천역, 낙성대역, 5);
        Sections sections = new Sections(section);
        // then
        assertThatThrownBy(() -> sections.delete(낙성대역))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("구간이 오직 하나인 노선에서 역을 제거할 수 없습니다.");
    }

    @DisplayName("구간에서 맨 앞 혹은 맨 뒤 역을 제거한다.")
    @Test
    void delete() {
        // given
        Section section = new Section(봉천역, 낙성대역, 5);
        Section section2 = new Section(낙성대역, 신림역, 5);
        Sections sections = new Sections(List.of(section, section2));
        // when
        sections.delete(봉천역);
        // then
        List<Section> results = sections.getSections();
        assertThat(results).contains(
                new Section(낙성대역, 신림역, 5)
        );
    }

    @DisplayName("구간 사이에 있는 역을 제거한다.")
    @Test
    void deleteMiddleStation() {
        // given
        Section section = new Section(봉천역, 낙성대역, 5);
        Section section2 = new Section(낙성대역, 신림역, 5);
        Sections sections = new Sections(List.of(section, section2));
        // when
        sections.delete(낙성대역);
        // then
        List<Section> results = sections.getSections();
        assertThat(results).contains(
                new Section(봉천역, 신림역, 10)
        );
    }

    @DisplayName("한 섹션에서 다른 섹션과의 차집합을 구한다. (호출객체 - 인자객체)")
    @Test
    void getDifferentList() {
        Sections sections = new Sections(List.of(
                new Section(봉천역, 낙성대역, 5),
                new Section(낙성대역, 신림역, 5),
                new Section(신림역, 서울대입구역, 5)));

        Sections sections2 = new Sections(List.of(
                new Section(봉천역, 낙성대역, 5),
                new Section(낙성대역, 사당역, 2),
                new Section(사당역, 신림역, 3),
                new Section(신림역, 서울대입구역, 5)
        ));

        List<Section> differentListA = sections.getDifferentList(sections2);
        List<Section> differentListB = sections2.getDifferentList(sections);
        assertThat(differentListA).contains(
                new Section(낙성대역, 신림역, 5)
        );
        assertThat(differentListB).contains(
                new Section(낙성대역, 사당역, 2),
                new Section(사당역, 신림역, 3)
        );
    }

    @DisplayName("상행 -> 하행 순으로 Sections를 정렬한다.")
    @Test
    void getSortedStations() {
        // given
        Section 신림_봉천 = new Section(신림역, 봉천역, 5);
        Section 봉천_설입 = new Section(봉천역, 서울대입구역, 10);
        Section 설입_낙성대 = new Section(서울대입구역, 낙성대역, 8);
        Section 낙성대_사당 = new Section(낙성대역, 사당역, 20);
        Sections sections = new Sections(List.of(봉천_설입, 설입_낙성대, 신림_봉천, 낙성대_사당));
        // when
        sections.getSortedStations();
        List<Station> results = sections.getSortedStations();
        // then
        assertThat(results).containsExactly(신림역, 봉천역, 서울대입구역, 낙성대역, 사당역);
    }
}
