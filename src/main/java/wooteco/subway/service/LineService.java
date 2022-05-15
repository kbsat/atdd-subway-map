package wooteco.subway.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.LineRequest;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.SectionRequest;
import wooteco.subway.utils.StringFormat;

@Service
@Transactional
public class LineService {

    private static final String LINE_DUPLICATION_EXCEPTION_MESSAGE = "이름이 중복되는 지하철 노선이 존재합니다.";
    private static final String NO_SUCH_LINE_EXCEPTION_MESSAGE = "해당 ID의 지하철 노선이 존재하지 않습니다.";

    private final LineDao lineDao;
    private final SectionDao sectionDao;
    private final StationDao stationDao;

    public LineService(LineDao lineDao, SectionDao sectionDao, StationDao stationDao) {
        this.lineDao = lineDao;
        this.sectionDao = sectionDao;
        this.stationDao = stationDao;
    }

    public LineResponse save(LineRequest lineRequest) {
        if (isDuplicateName(lineRequest)) {
            throw new IllegalArgumentException(
                    StringFormat.errorMessage(lineRequest.getName(), LINE_DUPLICATION_EXCEPTION_MESSAGE));
        }

        Line newLine = lineDao.save(lineRequest.toEntity());
        Station up = findExistStationById(lineRequest.getUpStationId());
        Station down = findExistStationById(lineRequest.getDownStationId());

        sectionDao.save(newLine.getId(), new Section(up, down, lineRequest.getDistance()));
        return LineResponse.of(newLine, List.of(up, down));
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        List<LineResponse> lineResponses = new ArrayList<>();
        List<Line> lines = lineDao.findAll();

        for (Line line : lines) {
            List<Section> findSections = sectionDao.findAllByLineId(line.getId());
            Sections sections = new Sections(findSections);
            lineResponses.add(LineResponse.of(line, sections.getSortedStations()));
        }

        return lineResponses;
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        Line findLine = findExistLineById(id);
        List<Section> findSections = sectionDao.findAllByLineId(findLine.getId());
        Sections sections = new Sections(findSections);

        List<Station> sortedStations = sections.getSortedStations();
        return LineResponse.of(findLine, sortedStations);
    }

    public void update(Long id, LineRequest lineRequest) {
        Line findLine = findExistLineById(id);
        if (isDuplicateName(lineRequest) && !findLine.isSameName(lineRequest.getName())) {
            throw new IllegalArgumentException(
                    StringFormat.errorMessage(lineRequest.getName(), LINE_DUPLICATION_EXCEPTION_MESSAGE));
        }

        lineDao.update(findLine.getId(), lineRequest.toEntity());
    }

    public void addSection(Long lineId, SectionRequest sectionRequest) {
        Line line = findExistLineById(lineId);
        List<Section> findSections = sectionDao.findAllByLineId(line.getId());

        Sections origin = new Sections(findSections);
        Section newSection = getSectionByRequest(sectionRequest);
        Sections resultSections = new Sections(findSections);
        resultSections.insert(newSection);

        deleteAndSaveSections(lineId, origin, resultSections);
    }

    private Section getSectionByRequest(SectionRequest sectionRequest) {
        Station up = findExistStationById(sectionRequest.getUpStationId());
        Station down = findExistStationById(sectionRequest.getDownStationId());

        return new Section(up, down, sectionRequest.getDistance());
    }

    private void deleteAndSaveSections(Long lineId, Sections origin, Sections resultSections) {
        List<Section> createdSections = resultSections.getDifferentList(origin);
        List<Section> toDeleteSections = origin.getDifferentList(resultSections);

        for (Section deleteTargetSection : toDeleteSections) {
            sectionDao.remove(deleteTargetSection);
        }
        for (Section createdSection : createdSections) {
            sectionDao.save(lineId, createdSection);
        }
    }

    public void delete(Long id) {
        lineDao.delete(findExistLineById(id));
    }

    public void deleteSection(Long lineId, Long stationId) {
        Line line = findExistLineById(lineId);
        Station stationToDelete = findExistStationById(stationId);
        List<Section> savedSections = sectionDao.findAllByLineId(line.getId());

        Sections origin = new Sections(savedSections);
        Sections results = new Sections(savedSections);
        results.delete(stationToDelete);

        deleteAndSaveSections(lineId, origin, results);
    }

    private Line findExistLineById(Long id) {
        return lineDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        StringFormat.errorMessage(id, NO_SUCH_LINE_EXCEPTION_MESSAGE)));
    }

    private Station findExistStationById(Long id) {
        return stationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        StringFormat.errorMessage(id, "해당 ID의 지하철역이 존재하지 않습니다.")));
    }

    private boolean isDuplicateName(LineRequest request) {
        return lineDao.findByName(request.getName()).isPresent();
    }
}
