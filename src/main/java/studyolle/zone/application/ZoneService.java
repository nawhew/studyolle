package studyolle.zone.application;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import studyolle.account.dto.ZoneForm;
import studyolle.zone.domain.Zone;
import studyolle.zone.domain.ZoneRepository;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private static final String ZONE_DATA_FILE_PATH = "list_of_city_in_korea.csv";

    private final ZoneRepository zoneRepository;

    /**
     * Zone에 데이터가 없는 경우 초기 데이터를 넣어줍니다
     * @throws IOException 
     */
    @PostConstruct
    public void initZoneData() throws IOException {
        if(this.zoneRepository.count() == 0) {
            this.zoneRepository.saveAll(readZoneDataFile());
        }
    }

    /**
     * 파일을 읽어 Zone 데이터를 리스트로 반환합니다.
     * @return
     * @throws IOException
     */
    private List<Zone> readZoneDataFile() throws IOException {
        Resource resource = new ClassPathResource(ZONE_DATA_FILE_PATH);
        return Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                .map(line -> {
                    String[] splitLine = line.split(",");
                    return Zone.builder()
                            .city(splitLine[0])
                            .localNameOfCity(splitLine[1])
                            .province(splitLine[2])
                            .build();
                }).collect(Collectors.toList());
    }


    public List<Zone> findAllZones() {
        return this.zoneRepository.findAll();
    }

    public Optional<Zone> findByZoneForm(ZoneForm zoneForm) {
        Zone zone = zoneForm.toEntity();
        return this.zoneRepository.findByCityAndProvince(zone.getCity(), zone.getProvince());
    }
}
