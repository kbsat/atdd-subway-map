package wooteco.subway.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Sections {

    private static final int MIDDLE_REMOVE_SIZE = 2;
    private static final int MIN_SECTION_SIZE = 1;

    private final List<Section> sections;

    public Sections(Section section) {
        this(List.of(section));
    }

    public Sections(List<Section> sections) {
        validateSize(sections);
        this.sections = new ArrayList<>(sections);
    }

    private void validateSize(List<Section> sections) {
        if (sections.isEmpty()) {
            throw new IllegalStateException("구간은 하나 이상 존재해야 합니다.");
        }
    }

    public void add(Section section) {
        List<Section> connectableSections = findConnectableSections(section);
        validateIncludingStations(section, connectableSections);

        Optional<Section> optionalSameUpOrDownStationSection = connectableSections.stream()
                .filter(sec -> sec.isSameUpOrDownStation(section))
                .findFirst();

        if (optionalSameUpOrDownStationSection.isPresent()) {
            Section originSection = optionalSameUpOrDownStationSection.get();
            validateDistance(originSection, section);
            addMiddleSection(originSection, section);
            return;
        }
        sections.add(section);
    }

    private void addMiddleSection(Section originSection, Section insertSection) {
        sections.remove(originSection);
        sections.add(insertSection);
        sections.add(originSection.slice(insertSection));
    }

    private void validateDistance(Section originSection, Section insertSection) {
        if (originSection.isShortAndEqualDistanceThan(insertSection)) {
            throw new IllegalArgumentException("역 사이에 구간을 등록할 경우 기존 역 구간 길이보다 짧아야 합니다.");
        }
    }

    private void validateIncludingStations(Section section, List<Section> connectableSections) {
        boolean haveSameUpStation = connectableSections.stream()
                .anyMatch(sec -> sec.haveUpStation(section));
        boolean haveSameDownStation = connectableSections.stream()
                .anyMatch(sec -> sec.haveDownStation(section));

        if (haveSameUpStation && haveSameDownStation) {
            throw new IllegalArgumentException("해당 구간은 기존 노선에 이미 등록되어있습니다.");
        }
    }

    private List<Section> findConnectableSections(Section section) {
        List<Section> connectableSections = sections.stream()
                .filter(sec -> sec.haveAnyStation(section))
                .collect(Collectors.toList());
        validateNoSameStationsInSection(connectableSections);

        return connectableSections;
    }

    private void validateNoSameStationsInSection(List<Section> connectableSections) {
        if (connectableSections.isEmpty()) {
            throw new IllegalArgumentException("상행역 또는 하행역이 노선에 포함되어 있어야합니다.");
        }
    }

    public void delete(Station station) {
        List<Section> removableSection = findRemovableSections(station);
        validateIncludingStation(removableSection);
        validateSectionSize();

        if (removableSection.size() == MIDDLE_REMOVE_SIZE) {
            removeInMiddle(station, removableSection);
        }

        sections.remove(removableSection.get(0));
    }

    public List<Section> getDifferentList(Sections otherSections) {
        List<Section> thisSections = new ArrayList<>(this.sections);
        thisSections.removeAll(otherSections.sections);

        return thisSections;
    }

    private List<Section> findRemovableSections(Station station) {
        return sections.stream()
                .filter(section -> section.haveStation(station))
                .collect(Collectors.toList());
    }

    private void removeInMiddle(Station station, List<Section> removableSection) {
        Section section1 = removableSection.get(0);
        Section section2 = removableSection.get(1);

        sections.add(section1.combine(section2, station));
        sections.remove(section1);
        sections.remove(section2);
    }

    private void validateIncludingStation(List<Section> removableSection) {
        if (removableSection.isEmpty()) {
            throw new IllegalArgumentException("지우려는 역이 노선에 포함되어 있어야합니다.");
        }
    }

    private void validateSectionSize() {
        if (sections.size() == MIN_SECTION_SIZE) {
            throw new IllegalStateException("구간이 오직 하나인 노선에서 역을 제거할 수 없습니다.");
        }
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<Station> getSortedStations() {
        List<Station> upStations = getUpStations();
        List<Station> downStations = getDownStations();

        Station firstStation = findFirstStation(upStations, downStations);
        Station endStation = findEndStation(upStations, downStations);

        return sortStations(firstStation, endStation);
    }

    public Sections copy() {
        return new Sections(new ArrayList<>(this.sections));
    }

    private List<Station> sortStations(Station firstStation, Station endStation) {
        List<Station> results = new ArrayList<>();
        results.add(firstStation);
        Section target = findSectionIncludingUpStation(firstStation);

        while (!target.isSameDownStation(endStation)) {
            Station targetDownStation = target.getDownStation();
            results.add(targetDownStation);
            target = findSectionIncludingUpStation(targetDownStation);
        }
        results.add(endStation);

        return results;
    }

    private Section findSectionIncludingUpStation(Station firstStation) {
        return sections.stream()
                .filter(section -> section.isSameUpStation(firstStation))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("찾으려는 구간이 존재하지 않습니다."));
    }

    private Station findEndStation(List<Station> upStations, List<Station> downStations) {
        downStations.removeAll(upStations);

        return downStations.get(0);
    }

    private Station findFirstStation(List<Station> upStations, List<Station> downStations) {
        return upStations.stream()
                .filter(station -> !downStations.contains(station))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("상행 종점이 존재하지 않습니다."));
    }

    private List<Station> getDownStations() {
        return sections.stream()
                .map(Section::getDownStation)
                .collect(Collectors.toList());
    }

    private List<Station> getUpStations() {
        return sections.stream()
                .map(Section::getUpStation)
                .collect(Collectors.toList());
    }
}
