package gov.va.api.lighthouse.facilities.collector;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Builder
@Slf4j
final class CemeteriesCollector {
  @NonNull final String baseUrl;

  @NonNull final RestTemplate insecureRestTemplate;

  @NonNull private final Map<String, String> websites;

  private final JdbcTemplate jdbcTemplate;

  /** Convert the results into a CdwCemetery Object. */
  @SneakyThrows
  static CdwCemetery toCdwCemetery(ResultSet rs) {
    return CdwCemetery.builder()
        .siteId(rs.getString("SITE_ID"))
        .fullName(rs.getString("FULL_NAME"))
        .siteType(rs.getString("SITE_TYPE"))
        .siteAddress1(rs.getString("SITE_ADDRESS1"))
        .siteAddress2(rs.getString("SITE_ADDRESS2"))
        .siteCity(rs.getString("SITE_CITY"))
        .siteState(rs.getString("SITE_STATE"))
        .siteZip(rs.getString("SITE_ZIP"))
        .mailAddress1(rs.getString("MAIL_ADDRESS1"))
        .mailAddress2(rs.getString("MAIL_ADDRESS2"))
        .mailCity(rs.getString("MAIL_CITY"))
        .mailState(rs.getString("MAIL_STATE"))
        .mailZip(rs.getString("MAIL_ZIP"))
        .phone(rs.getString("PHONE"))
        .fax(rs.getString("FAX"))
        .visitationHoursWeekday(rs.getString("VISITATION_HOURS_WEEKDAY"))
        .visitationHoursWeekend(rs.getString("VISITATION_HOURS_WEEKEND"))
        .latitude(rs.getBigDecimal("LATITUDE_DD"))
        .longitude(rs.getBigDecimal("LONGITUDE_DD"))
        .websiteUrl(rs.getString("Website_URL"))
        .build();
  }

  /** Collects and transforms all national cemeteries into a list of facilities. */
  public Collection<Facility> collect() {
    List<NationalCemeteries.NationalCemetery> cemeteries = xmlCemeteries();
    try {
      return queryCdwCemeteries().stream()
          .filter(c -> !equalsIgnoreCase(c.siteType(), "office"))
          .map(
              facility ->
                  CemeteriesTransformer.builder()
                      .cdwFacility(facility)
                      .facilityName(xmlFacilityName(cemeteries, facility.siteId()))
                      .website(xmlOrCsvWebsite(cemeteries, facility.siteId()))
                      .build()
                      .toFacility())
          .collect(toList());
    } catch (Exception e) {
      throw new CollectorExceptions.CemeteriesCollectorException(e);
    }
  }

  /** Requests CDW cemetery in a List. */
  @SneakyThrows
  private List<CdwCemetery> queryCdwCemeteries() {
    final Stopwatch totalWatch = Stopwatch.createStarted();
    List<CdwCemetery> cdwCemeteries =
        ImmutableList.copyOf(
            jdbcTemplate.query(
                "SELECT "
                    + "SITE_ID,"
                    + "FULL_NAME,"
                    + "SITE_TYPE,"
                    + "SITE_ADDRESS1,"
                    + "SITE_ADDRESS2,"
                    + "SITE_CITY,"
                    + "SITE_STATE,"
                    + "SITE_ZIP,"
                    + "MAIL_ADDRESS1,"
                    + "MAIL_ADDRESS2,"
                    + "MAIL_CITY,"
                    + "MAIL_STATE,"
                    + "MAIL_ZIP,"
                    + "PHONE,"
                    + "FAX,"
                    + "VISITATION_HOURS_WEEKDAY,"
                    + "VISITATION_HOURS_WEEKEND,"
                    + "LATITUDE_DD,"
                    + "LONGITUDE_DD,"
                    + "Website_URL"
                    + " FROM App.FacilityLocator_NCA",
                (rs, rowNum) -> toCdwCemetery(rs)));
    log.info(
        "Loading cemeteries took {} millis for {} entries",
        totalWatch.stop().elapsed(TimeUnit.MILLISECONDS),
        cdwCemeteries.size());
    checkState(!cdwCemeteries.isEmpty(), "No App.FacilityLocator_NCA entries");
    return cdwCemeteries;
  }

  @SneakyThrows
  private List<NationalCemeteries.NationalCemetery> xmlCemeteries() {
    Stopwatch totalWatch = Stopwatch.createStarted();
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "cems/national.xml").build().toUriString();
    String response =
        insecureRestTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class)
            .getBody();
    List<NationalCemeteries.NationalCemetery> cemeteries =
        new XmlMapper()
            .registerModule(new StringTrimModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .readValue(response, NationalCemeteries.class)
            .cem();
    log.info(
        "Loading national cemeteries xml took {} millis for {} entries",
        totalWatch.stop().elapsed(TimeUnit.MILLISECONDS),
        cemeteries.size());
    checkState(!cemeteries.isEmpty(), "No cems/national.xml entries!");
    return cemeteries;
  }

  private String xmlFacilityName(
      List<NationalCemeteries.NationalCemetery> cemeteries, String siteId) {
    for (NationalCemeteries.NationalCemetery cem : cemeteries) {
      if (cem.id.equals(siteId)) {
        return cem.name;
      }
    }
    return null;
  }

  private String xmlOrCsvWebsite(
      List<NationalCemeteries.NationalCemetery> cemeteries, String siteId) {
    for (NationalCemeteries.NationalCemetery cem : cemeteries) {
      if (cem.id.equals(siteId)) {
        return cem.url;
      }
    }
    return websites.get("nca_" + siteId);
  }

  private static final class StringTrimModule extends SimpleModule {
    StringTrimModule() {
      addDeserializer(
          String.class,
          new StdScalarDeserializer<>(String.class) {
            @Override
            @SneakyThrows
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
              return trimToNull(p.getValueAsString());
            }
          });
    }
  }
}
