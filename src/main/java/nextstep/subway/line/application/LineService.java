package nextstep.subway.line.application;

import lombok.RequiredArgsConstructor;
import nextstep.subway.line.LineNotFoundException;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.section.SectionNotFoundException;
import nextstep.subway.section.domain.Section;
import nextstep.subway.section.domain.SectionRepository;
import nextstep.subway.section.domain.Sections;
import nextstep.subway.section.dto.SectionRequest;
import nextstep.subway.section.dto.SectionResponse;
import nextstep.subway.station.StationNotFoundException;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LineService {
    private final LineRepository lineRepository;
    private final SectionRepository sectionRepository;
    private final StationRepository stationRepository;

    @Transactional(readOnly = true)
    public Line findLineById(Long lineId) {
        return lineRepository.findById(lineId).orElseThrow(LineNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Section findSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId).orElseThrow(SectionNotFoundException::new);

    }
    @Transactional(readOnly = true)
    public Station findStationById(Long stationId) {
        return stationRepository.findById(stationId).orElseThrow(StationNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAllLines() {
        List<Line> Lines = lineRepository.findAll();

        return Lines.stream()
            .map(LineResponse::of)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findAllSections() {
        List<Section> sections = sectionRepository.findAll();

        return sections.stream()
                .map(SectionResponse::of)
                .collect(Collectors.toList());
    }

    public LineResponse findLine(final Long id) {
        Line line = findLineById(id);

        return LineResponse.of(line);
    }

    public SectionResponse findSection(final Long id) {
        Section section = findSectionById(id);
        return SectionResponse.of(section);
    }

    public LineResponse saveLine(final LineRequest request) {
        Line persistLine = lineRepository.save(request.toLine());

        registerSection(request.toSectionRequest(), persistLine);

        return LineResponse.of(persistLine);
    }

    public SectionResponse appendSection(final Long id, final SectionRequest request) {
        Line line = findLineById(id);

        Section section = registerSection(request, line);

        return SectionResponse.of(section);
    }

    public void updateLine(final Long id, final LineRequest request) {
        Line line = findLineById(id);

        line.update(request.toLine());
    }

    public void deleteLineById(final Long id) {
        lineRepository.deleteById(id);
    }

    public void deleteSectionByStationId(final Long lineId, final Long stationId) {
        Line line = findLineById(lineId);

        Station station = findStationById(stationId);

        Sections sections = line.getSections();
        sections.remove(station);
    }

    private Section registerSection(final SectionRequest request, final Line line) {
        Station upStation = findStationById(request.getUpStationId());
        Station downStation = findStationById(request.getDownStationId());

        Section section = Section.builder()
                .upStation(upStation)
                .downStation(downStation)
                .distance(request.getDistance())
                .build();

        section.registerLine(line);

        return sectionRepository.save(section);
    }
}
