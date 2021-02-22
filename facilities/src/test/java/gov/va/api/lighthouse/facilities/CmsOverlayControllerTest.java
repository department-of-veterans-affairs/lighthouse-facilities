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
                    .changed("2021-02-04T22:36:49+00:00")
                    .descriptionNational("Vaccine availability for COVID-19")
                    .descriptionSystem("System description for vaccine availability for COVID-19")
                    .descriptionFacility(
                        "Facility description for vaccine availability for COVID-19")
                    .healthServiceApiId("12345")
                    .appointmentLeadIn(
                        "Your VA health care team will contact you if you...more text")
                    .onlineSchedulingAvailable(0)
                    .path(
                        "\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines")
                    .phoneNumbers(
                        List.of(
                            DetailedService.DetailedServicePhoneNumber.builder()
                                .extension("123")
                                .label("Main phone")
                                .number("555-555-1212")
                                .type("tel")
                                .build()))
                    .referralRequired(0)
                    .walkInsAccepted(1)
                    .serviceLocations(
                        List.of(
                            DetailedService.DetailedServiceLocation.builder()
                                .serviceLocationAddress(
                                    DetailedService.DetailedServiceAddress.builder()
                                        .buildingNameNumber("Baxter Building")
                                        .clinicName("Baxter Clinic")
                                        .wingFloorOrRoomNumber("Wing East")
                                        .address1("122 Main St.")
                                        .address2(null)
                                        .city("Rochester")
                                        .state("NY")
                                        .zipCode("14623-1345")
                                        .countryCode("US")
                                        .build())
                                .phoneNumbers(
                                    List.of(
                                        DetailedService.DetailedServicePhoneNumber.builder()
                                            .extension("567")
                                            .label("Alt phone")
                                            .number("556-565-1119")
                                            .type("tel")
                                            .build()))
                                .emailContacts(
                                    List.of(
                                        DetailedService.DetailedServiceEmailContact.builder()
                                            .emailAddress("georgea@va.gov")
                                            .mailTo("George Anderson")
                                            .build()))
                                .facilityServiceHours(
                                    DetailedService.DetailedServiceHours.builder()
                                        .monday("8:30AM-7:00PM")
                                        .tuesday("8:30AM-7:00PM")
                                        .wednesday("8:30AM-7:00PM")
                                        .thursday("8:30AM-7:00PM")
                                        .friday("8:30AM-7:00PM")
                                        .saturday("8:30AM-7:00PM")
                                        .sunday("CLOSED")
                                        .build())
                                .additionalHoursInfo("Please call for an appointment outside...")
                                .build()))
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
