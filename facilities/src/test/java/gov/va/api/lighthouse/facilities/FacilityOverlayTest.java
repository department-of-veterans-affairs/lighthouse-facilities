package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.cms.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.Facility.ActiveStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatusCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class FacilityOverlayTest {
  private static final ObjectMapper mapper = FacilitiesJacksonConfig.createMapper();

  @Test
  void activeStatusIsPopulatedByOperatingStatusWhenAvailable() {
    assertStatus(
        ActiveStatus.A,
        op(OperatingStatusCode.NORMAL, "neato"),
        null,
        entity(
            fromActiveStatus(ActiveStatus.T), overlay(op(OperatingStatusCode.NORMAL, "neato"), 0)));
    assertStatus(
        ActiveStatus.A,
        op(OperatingStatusCode.NOTICE, "neato"),
        null,
        entity(
            fromActiveStatus(ActiveStatus.T), overlay(op(OperatingStatusCode.NOTICE, "neato"), 0)));
    assertStatus(
        ActiveStatus.A,
        op(OperatingStatusCode.LIMITED, "neato"),
        List.of(Facility.HealthService.Covid19Vaccine),
        entity(
            fromActiveStatus(ActiveStatus.T),
            overlay(op(OperatingStatusCode.LIMITED, "neato"), 1)));
    assertStatus(
        ActiveStatus.T,
        op(OperatingStatusCode.CLOSED, "neato"),
        List.of(Facility.HealthService.Covid19Vaccine),
        entity(
            fromActiveStatus(ActiveStatus.A), overlay(op(OperatingStatusCode.CLOSED, "neato"), 1)));
  }

  private void assertStatus(
      ActiveStatus expectedActiveStatus,
      OperatingStatus expectedOperatingStatus,
      List<Facility.HealthService> expectedHealthServices,
      FacilityEntity entity) {
    Facility facility = FacilityOverlay.builder().mapper(mapper).build().apply(entity);
    assertThat(facility.attributes().activeStatus()).isEqualTo(expectedActiveStatus);
    assertThat(facility.attributes().operatingStatus()).isEqualTo(expectedOperatingStatus);
    assertThat(facility.attributes().services().health()).isEqualTo(expectedHealthServices);
  }

  @Test
  void covid19VaccineIsPopulatedWhenAvailable() {
    assertStatus(
        null,
        OperatingStatus.builder().code(OperatingStatusCode.NORMAL).build(),
        List.of(Facility.HealthService.Covid19Vaccine),
        entity(fromActiveStatus(null), overlay(null, 1)));
    assertStatus(
        null,
        OperatingStatus.builder().code(OperatingStatusCode.NORMAL).build(),
        null,
        entity(fromActiveStatus(null), overlay(null, 0)));
  }

  @SneakyThrows
  private FacilityEntity entity(Facility facility, CmsOverlay overlay) {

    Set<String> detailedServices = null;

    if (overlay != null) {
      detailedServices = new HashSet<>();
      for (DetailedService service : overlay.detailedServices()) {
        if (1 == service.active()) {
          detailedServices.add(service.name());
        }
      }
    }

    return FacilityEntity.builder()
        .facility(mapper.writeValueAsString(facility))
        .cmsOperatingStatus(
            overlay == null ? null : mapper.writeValueAsString(overlay.operatingStatus()))
        .overlayServices(overlay == null ? null : detailedServices)
        .build();
  }

  private Facility fromActiveStatus(ActiveStatus status) {
    return Facility.builder()
        .attributes(FacilityAttributes.builder().activeStatus(status).build())
        .build();
  }

  private OperatingStatus op(OperatingStatusCode code, String info) {
    return OperatingStatus.builder().code(code).additionalInfo(info).build();
  }

  @Test
  void operatingStatusIsPopulatedByActiveStatusWhenNotAvailable() {
    assertStatus(
        ActiveStatus.A,
        OperatingStatus.builder().code(OperatingStatusCode.NORMAL).build(),
        null,
        entity(fromActiveStatus(ActiveStatus.A), null));
    assertStatus(
        ActiveStatus.T,
        OperatingStatus.builder().code(OperatingStatusCode.CLOSED).build(),
        null,
        entity(fromActiveStatus(ActiveStatus.T), null));
    assertStatus(
        null,
        OperatingStatus.builder().code(OperatingStatusCode.NORMAL).build(),
        null,
        entity(fromActiveStatus(null), null));
  }

  private CmsOverlay overlay(OperatingStatus neato, int cmsServiceActiveValue) {
    return CmsOverlay.builder()
        .operatingStatus(neato)
        .detailedServices(
            List.of(
                DetailedService.builder()
                    .name("Covid19Vaccine")
                    .active(cmsServiceActiveValue)
                    .descriptionNational("Vaccine availability for COVID-19")
                    .descriptionSystem("System description for vaccine availability for COVID-19")
                    .descriptionFacility(
                        "Facility description for vaccine availability for COVID-19")
                    .healthServiceApiId("12345")
                    .build()))
        .build();
  }
}
