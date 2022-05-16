package wooteco.subway.domain;

import java.util.Objects;

public class Section {

    private final Long id;
    private final Station upStation;
    private final Station downStation;
    private final int distance;

    public Section(Station upStation, Station downStation, int distance) {
        this(null, upStation, downStation, distance);
    }

    public Section(Long id, Station upStation, Station downStation, int distance) {
        Objects.requireNonNull(upStation);
        Objects.requireNonNull(downStation);
        validateDistanceOverZero(distance);

        this.id = id;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public Section slice(Section insertSection) {
        if (isSameDownStation(insertSection.downStation)) {
            return new Section(upStation, insertSection.upStation, distance - insertSection.distance);
        }

        return new Section(insertSection.downStation, downStation, distance - insertSection.distance);
    }

    public Section combine(Section target, Station connectStation) {
        validateHavingConnectStation(target, connectStation);
        if (this.isSameDownStation(connectStation)) {
            return new Section(upStation, target.downStation, distance + target.distance);
        }

        return new Section(target.upStation, downStation, distance + target.distance);
    }

    private void validateHavingConnectStation(Section target, Station connectStation) {
        if (!this.haveStation(connectStation) || !target.haveStation(connectStation)) {
            throw new IllegalArgumentException("양쪽 구간에 겹치는 역을 올바르게 지정해야합니다.");
        }
    }

    private void validateDistanceOverZero(int distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("거리는 0 이하가 될 수 없습니다.");
        }
    }

    boolean isSameUpStation(Station station) {
        return upStation.equals(station);
    }

    boolean isSameDownStation(Station station) {
        return downStation.equals(station);
    }

    boolean haveStation(Station station) {
        return isSameUpStation(station) || isSameDownStation(station);
    }

    boolean haveAnyStation(Section section) {
        return haveUpStation(section) || haveDownStation(section);
    }

    boolean isSameUpOrDownStation(Section section) {
        return isSameUpStation(section.upStation) || isSameDownStation(section.downStation);
    }

    boolean haveUpStation(Section section) {
        return isSameUpStation(section.upStation) || isSameDownStation(section.upStation);
    }

    boolean haveDownStation(Section section) {
        return isSameUpStation(section.downStation) || isSameDownStation(section.downStation);
    }

    boolean isShortAndEqualDistanceThan(Section section) {
        return distance <= section.distance;
    }

    public Long getId() {
        return id;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Section section = (Section) o;
        return distance == section.distance && Objects.equals(upStation, section.upStation)
                && Objects.equals(downStation, section.downStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upStation, downStation, distance);
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", upStation=" + upStation +
                ", downStation=" + downStation +
                ", distance=" + distance +
                '}';
    }
}
