package gov.va.api.lighthouse.facilities.collector;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacilitiesCollector {
  private final InsecureRestTemplateProvider insecureRestTemplateProvider;

  private final JdbcTemplate jdbcTemplate;

  private final String atcBaseUrl;

  private final String atpBaseUrl;

  private final String cemeteriesBaseUrl;

  /** Autowired constructor. */
  public FacilitiesCollector(
      @Autowired InsecureRestTemplateProvider insecureRestTemplateProvider,
      @Autowired JdbcTemplate jdbcTemplate,
      @Value("${access-to-care.url}") String atcBaseUrl,
      @Value("${access-to-pwt.url}") String atpBaseUrl,
      @Value("${cemeteries.url}") String cemeteriesBaseUrl) {
    this.insecureRestTemplateProvider = insecureRestTemplateProvider;
    this.jdbcTemplate = jdbcTemplate;
    this.atcBaseUrl = withTrailingSlash(atcBaseUrl);
    this.atpBaseUrl = withTrailingSlash(atpBaseUrl);
    this.cemeteriesBaseUrl = withTrailingSlash(cemeteriesBaseUrl);
  }

  @SneakyThrows
  private static Map<String, String> loadWebsites() {
    final Stopwatch totalWatch = Stopwatch.createStarted();
    try (InputStreamReader reader =
        new InputStreamReader(
            new ClassPathResource("websites.csv").getInputStream(), StandardCharsets.UTF_8)) {
      Iterable<CSVRecord> rows = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      for (CSVRecord row : rows) {
        String id = trimToNull(row.get("id"));
        String url = trimToNull(row.get("url"));
        checkState(id != null, "Website %s missing ID", url);
        checkState(url != null, "Website %s missing url", id);
        checkState(!map.containsKey(id), "Website %s duplicate", id);
        map.put(id, url);
      }
      Map<String, String> websites = Collections.unmodifiableMap(map);
      log.info(
          "Loading websites took {} millis for {} entries",
          totalWatch.stop().elapsed(TimeUnit.MILLISECONDS),
          websites.size());
      checkState(!websites.isEmpty(), "No website entries");
      return websites;
    }
  }

  @SneakyThrows
  static VastEntity toVastEntity(ResultSet rs) {
    return VastEntity.builder()
        .vetCenter(rs.getBoolean("VCTR2"))
        .mobileVetCenter(rs.getBoolean("MVCTR"))
        .latitude(rs.getBigDecimal("LAT"))
        .longitude(rs.getBigDecimal("LON"))
        .stationNumber(rs.getString("STA_NO"))
        .stationName(rs.getString("STATIONNAME"))
        .abbreviation(rs.getString("S_ABBR"))
        .cocClassificationId(rs.getString("COCCLASSIFICATIONID"))
        .address1(rs.getString("ADDRESS1"))
        .address2(rs.getString("ADDRESS2"))
        .address3(rs.getString("ADDRESS3"))
        .city(rs.getString("CITY"))
        .state(rs.getString("ST"))
        .zip(rs.getString("zip"))
        .zip4(rs.getString("ZIP4"))
        .monday(rs.getString("MONDAY"))
        .tuesday(rs.getString("TUESDAY"))
        .wednesday(rs.getString("WEDNESDAY"))
        .thursday(rs.getString("THURSDAY"))
        .friday(rs.getString("FRIDAY"))
        .saturday(rs.getString("SATURDAY"))
        .sunday(rs.getString("SUNDAY"))
        .staPhone(rs.getString("STA_PHONE"))
        .staFax(rs.getString("STA_FAX"))
        .afterHoursPhone(rs.getString("AFTERHOURSPHONE"))
        .patientAdvocatePhone(rs.getString("PATIENTADVOCATEPHONE"))
        .enrollmentCoordinatorPhone(rs.getString("ENROLLMENTCOORDINATORPHONE"))
        .pharmacyPhone(rs.getString("PHARMACYPHONE"))
        .pod(rs.getString("POD"))
        .mobile(rs.getBoolean("MOBILE"))
        .visn(rs.getString("VISN"))
        .lastUpdated(
            Optional.ofNullable(rs.getTimestamp("LASTUPDATED"))
                .map(t -> t.toInstant())
                .orElse(null))
        .operationalHoursSpecialInstructions(rs.getString("OPERATIONALHOURSSPECIALINSTRUCTIONS"))
        .build();
  }

  static String withTrailingSlash(@NonNull String url) {
    return url.endsWith("/") ? url : url + "/";
  }

  /** Collect facilities. */
  @SneakyThrows
  public List<Facility> collectFacilities() {
    Map<String, String> websites;
    Collection<VastEntity> vastEntities;
    try {
      websites = loadWebsites();
      vastEntities = loadVast();
    } catch (Exception e) {
      throw new CollectorExceptions.CollectorException(e);
    }

    Collection<Facility> healths =
        HealthsCollector.builder()
            .atcBaseUrl(atcBaseUrl)
            .atpBaseUrl(atpBaseUrl)
            .jdbcTemplate(jdbcTemplate)
            .insecureRestTemplate(insecureRestTemplateProvider.restTemplate())
            .vastEntities(vastEntities)
            .websites(websites)
            .build()
            .collect();

    Collection<Facility> stateCems =
        StateCemeteriesCollector.builder()
            .baseUrl(cemeteriesBaseUrl)
            .insecureRestTemplate(insecureRestTemplateProvider.restTemplate())
            .websites(websites)
            .build()
            .collect();

    Collection<Facility> vetCenters =
        VetCentersCollector.builder()
            .vastEntities(vastEntities)
            .websites(websites)
            .build()
            .collect();

    Collection<Facility> benefits =
        BenefitsCollector.builder().websites(websites).jdbcTemplate(jdbcTemplate).build().collect();

    Collection<Facility> cemeteries =
        CemeteriesCollector.builder()
            .baseUrl(cemeteriesBaseUrl)
            .insecureRestTemplate(insecureRestTemplateProvider.restTemplate())
            .websites(websites)
            .jdbcTemplate(jdbcTemplate)
            .build()
            .collect();

    log.info(
        "Collected: Health {},  Benefits {},  Vet centers {}, "
            + "Non-national cemeteries {}, Cemeteries {}",
        healths.size(),
        benefits.size(),
        vetCenters.size(),
        stateCems.size(),
        cemeteries.size());

    return Streams.stream(Iterables.concat(benefits, cemeteries, healths, stateCems, vetCenters))
        .sorted((left, right) -> left.id().compareToIgnoreCase(right.id()))
        .collect(toList());
  }

  private List<VastEntity> loadVast() {
    Stopwatch watch = Stopwatch.createStarted();
    List<VastEntity> entities =
        ImmutableList.copyOf(
            jdbcTemplate.query(
                "SELECT "
                    + "VCTR2,"
                    + "MVCTR,"
                    + "LAT,"
                    + "LON,"
                    + "STA_NO,"
                    + "STATIONNAME,"
                    + "S_ABBR,"
                    + "COCCLASSIFICATIONID,"
                    + "ADDRESS1,"
                    + "ADDRESS2,"
                    + "ADDRESS3,"
                    + "CITY,"
                    + "ST,"
                    + "ZIP,"
                    + "ZIP4,"
                    + "MONDAY,"
                    + "TUESDAY,"
                    + "WEDNESDAY,"
                    + "THURSDAY,"
                    + "FRIDAY,"
                    + "SATURDAY,"
                    + "SUNDAY,"
                    + "OPERATIONALHOURSSPECIALINSTRUCTIONS,"
                    + "STA_PHONE,"
                    + "STA_FAX,"
                    + "AFTERHOURSPHONE,"
                    + "PATIENTADVOCATEPHONE,"
                    + "ENROLLMENTCOORDINATORPHONE,"
                    + "PHARMACYPHONE,"
                    + "POD,"
                    + "MOBILE,"
                    + "VISN,"
                    + "LASTUPDATED"
                    + " FROM App.Vast",
                (rs, rowNum) -> toVastEntity(rs)));

    log.info(
        "Loading VAST took {} millis for {} entries",
        watch.stop().elapsed(TimeUnit.MILLISECONDS),
        entities.size());
    checkState(!entities.isEmpty(), "No App.Vast entries");
    return entities;
  }
}
