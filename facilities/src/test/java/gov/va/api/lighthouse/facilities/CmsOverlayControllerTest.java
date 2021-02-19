package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.cms.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatusCode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CmsOverlayControllerTest {
  @Mock FacilityRepository repository;

  CmsOverlayController controller() {
    return CmsOverlayController.builder().repository(repository).build();
  }

  private CmsOverlay overlay() {
    return CmsOverlay.builder()
        .operatingStatus(
            OperatingStatus.builder()
                .code(OperatingStatusCode.NOTICE)
                .additionalInfo("i need attention")
                .build())
        .detailedServices(
            List.of(
                DetailedService.builder()
                    .name("COVID-19 vaccines")
                    .active(1)
                    //                    .descriptionNational("Vaccine availability for COVID-19")
                    //                    .descriptionSystem("System description for vaccine
                    // availability for COVID-19")
                    .descriptionFacility(
                        "Facility description for vaccine availability for COVID-19")
                    .healthServiceApiId("12345")
                    .build()))
        .build();
  }

  @Test
  @SneakyThrows
  void updateIsAcceptedForKnownStation() {
    var pk = FacilityEntity.Pk.fromIdString("vha_123");
    FacilityEntity entity = FacilityEntity.builder().id(pk).build();
    when(repository.findById(pk)).thenReturn(Optional.of(entity));
    CmsOverlay overlay = overlay();
    ResponseEntity<Void> response = controller().saveOverlay("vha_123", overlay);
    Set<String> detailedServices = new HashSet<>();

    for (DetailedService service : overlay.detailedServices()) {
      if (1 == service.active()) {
        detailedServices.add(service.name());
      }
    }

    entity.cmsOperatingStatus(
        FacilitiesJacksonConfig.createMapper().writeValueAsString(overlay.operatingStatus()));
    entity.overlayServices(detailedServices);
    verify(repository).save(entity);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void updateIsSkippedForUnknownStation() {
    var pk = FacilityEntity.Pk.fromIdString("vha_666");
    when(repository.findById(pk)).thenReturn(Optional.empty());
    ResponseEntity<Void> response = controller().saveOverlay("vha_666", overlay());
    verifyNoMoreInteractions(repository);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }
}
