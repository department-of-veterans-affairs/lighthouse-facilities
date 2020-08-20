package gov.va.api.lighthouse.facilities;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.Facility.Address;
import gov.va.api.lighthouse.facilities.api.v0.Facility.Addresses;
import gov.va.api.lighthouse.facilities.api.v0.Facility.BenefitsService;
import gov.va.api.lighthouse.facilities.api.v0.Facility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OtherService;
import gov.va.api.lighthouse.facilities.api.v0.Facility.Services;
import gov.va.api.lighthouse.facilities.collector.FacilitiesCollector;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InternalFacilitiesControllerTest {
  @Autowired FacilityRepository facilityRepository;

  @Autowired FacilityGraveyardRepository graveyardRepository;

  FacilitiesCollector collector = mock(FacilitiesCollector.class);

  private static Facility _facility(
      String id,
      String state,
      String zip,
      double latitude,
      double longitude,
      List<HealthService> health) {
    return Facility.builder()
        .id(id)
        .attributes(
            FacilityAttributes.builder()
                .address(
                    Addresses.builder()
                        .physical(Address.builder().state(state).zip(zip).build())
                        .build())
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .services(Services.builder().health(health).build())
                .build())
        .build();
  }

  private static CmsOverlay _overlay() {
    return CmsOverlay.builder()
        .operatingStatus(
            Facility.OperatingStatus.builder()
                .code(Facility.OperatingStatusCode.LIMITED)
                .additionalInfo("Limited")
                .build())
        .build();
  }

  private InternalFacilitiesController _controller() {
    return InternalFacilitiesController.builder()
        .collector(collector)
        .facilityRepository(facilityRepository)
        .graveyardRepository(graveyardRepository)
        .build();
  }

  private FacilityEntity _entity(Facility fac) {
    return _entityWithOverlay(fac, null);
  }

  @SneakyThrows
  private FacilityEntity _entityWithOverlay(Facility fac, CmsOverlay overlay) {
    String o = overlay == null ? null : JacksonConfig.createMapper().writeValueAsString(overlay);
    return InternalFacilitiesController.populate(
        FacilityEntity.builder()
            .id(FacilityEntity.Pk.fromIdString(fac.id()))
            .cmsOverlay(o)
            .lastUpdated(Instant.now())
            .build(),
        fac);
  }

  @SneakyThrows
  private FacilityGraveyardEntity _graveyardEntityWithOverlay(Facility fac, CmsOverlay overlay) {
    String o = overlay == null ? null : JacksonConfig.createMapper().writeValueAsString(overlay);
    return FacilityGraveyardEntity.builder()
        .id(FacilityEntity.Pk.fromIdString(fac.id()))
        .facility(FacilitiesJacksonConfig.createMapper().writeValueAsString(fac))
        .cmsOverlay(o)
        .missingTimestamp(LocalDateTime.now().minusDays(4).toInstant(ZoneOffset.UTC).toEpochMilli())
        .lastUpdated(Instant.now())
        .build();
  }

  @Test
  @SneakyThrows
  void collect_createUpdate() {
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    Facility f2 = _facility("vha_f2", "NEAT", "32934", 5.6, 6.7, List.of(HealthService.UrgentCare));
    Facility f2Old =
        _facility("vha_f2", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    facilityRepository.save(_entity(f2Old));
    when(collector.collectFacilities()).thenReturn(List.of(f1, f2));
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesCreated()).isEqualTo(List.of("vha_f1"));
    assertThat(response.facilitiesUpdated()).isEqualTo(List.of("vha_f2"));
    assertThat(facilityRepository.findAll()).isEqualTo(List.of(_entity(f1), _entity(f2)));
  }

  @Test
  @SneakyThrows
  void collect_fromTheGraveyard() {
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    Facility f1Old =
        _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    FacilityGraveyardEntity entity = _graveyardEntityWithOverlay(f1Old, _overlay());
    graveyardRepository.save(entity);
    when(collector.collectFacilities()).thenReturn(List.of(f1));
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesRevived()).isEqualTo(List.of("vha_f1"));
    assertThat(graveyardRepository.findAll()).isEmpty();
    FacilityEntity result = Iterables.getOnlyElement(facilityRepository.findAll());
    assertThat(result.id()).isEqualTo(entity.id());
    assertThat(result.facility())
        .isEqualTo(FacilitiesJacksonConfig.createMapper().writeValueAsString(f1));
    assertThat(result.cmsOverlay()).isEqualTo(entity.cmsOverlay());
    assertThat(result.missingTimestamp()).isNull();
    assertThat(result.lastUpdated()).isEqualTo(response.timing().completeCollection());
  }

  @Test
  @SneakyThrows
  void collect_missing() {
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    Facility f1Old =
        _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    Facility f2Old =
        _facility("vha_f2", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    Facility f3Old =
        _facility("vha_f3", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    Facility f4Old =
        _facility("vha_f4", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    facilityRepository.save(_entity(f1Old));
    facilityRepository.save(_entity(f2Old));
    facilityRepository.save(_entity(f3Old));
    facilityRepository.save(_entity(f4Old));
    when(collector.collectFacilities()).thenReturn(List.of(f1));
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesUpdated()).isEqualTo(List.of("vha_f1"));
    assertThat(response.facilitiesMissing()).isEqualTo(List.of("vha_f2", "vha_f3", "vha_f4"));
    List<FacilityEntity> findAll = ImmutableList.copyOf(facilityRepository.findAll());
    assertThat(findAll).hasSize(4);
    assertThat(findAll.get(0).missingTimestamp()).isNull();
    assertThat(findAll.get(0).services()).isEqualTo(Set.of("MentalHealthCare"));
    assertThat(findAll.get(1).missingTimestamp()).isNotNull();
    assertThat(findAll.get(2).missingTimestamp()).isNotNull();
    assertThat(findAll.get(3).missingTimestamp()).isNotNull();
  }

  @Test
  @SneakyThrows
  void collect_missingComesBack() {
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    Facility f1Old =
        _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    facilityRepository.save(_entity(f1Old).missingTimestamp(Instant.now().toEpochMilli()));
    when(collector.collectFacilities()).thenReturn(List.of(f1));
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesUpdated()).isEqualTo(List.of("vha_f1"));
    FacilityEntity result = Iterables.getOnlyElement(facilityRepository.findAll());
    assertThat(result.missingTimestamp()).isNull();
    assertThat(result.services()).isEqualTo(Set.of("MentalHealthCare"));
  }

  @Test
  @SneakyThrows
  void collect_missingTimestampPreserved() {
    Facility f1Old =
        _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    long early = Instant.now().minusSeconds(60).toEpochMilli();
    facilityRepository.save(_entity(f1Old).missingTimestamp(early));
    when(collector.collectFacilities()).thenReturn(emptyList());
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesMissing()).isEqualTo(List.of("vha_f1"));
    FacilityEntity result = Iterables.getOnlyElement(facilityRepository.findAll());
    assertThat(result.missingTimestamp()).isEqualTo(early);
  }

  @Test
  @SneakyThrows
  void collect_noStateOrZip() {
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    f1.attributes().address().physical().state(null);
    f1.attributes().address().physical().zip(null);
    when(collector.collectFacilities()).thenReturn(List.of(f1));
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesCreated()).isEqualTo(List.of("vha_f1"));
    assertThat(response.problems())
        .isEqualTo(
            List.of(
                ReloadResponse.Problem.of("vha_f1", "Missing zip"),
                ReloadResponse.Problem.of("vha_f1", "Missing state")));
  }

  @Test
  @SneakyThrows
  void collect_toTheGraveyard() {
    Facility f1Old =
        _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    long threeDaysAgo = LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC).toEpochMilli();
    FacilityEntity entity = _entityWithOverlay(f1Old, _overlay()).missingTimestamp(threeDaysAgo);
    facilityRepository.save(entity);
    when(collector.collectFacilities()).thenReturn(emptyList());
    ReloadResponse response = _controller().reload().getBody();
    assertThat(response.facilitiesRemoved()).isEqualTo(List.of("vha_f1"));
    assertThat(facilityRepository.findAllIds()).isEmpty();
    FacilityGraveyardEntity result = Iterables.getOnlyElement(graveyardRepository.findAll());
    assertThat(result.id()).isEqualTo(entity.id());
    assertThat(result.facility()).isEqualTo(entity.facility());
    assertThat(result.cmsOverlay()).isEqualTo(entity.cmsOverlay());
    assertThat(result.missingTimestamp()).isEqualTo(threeDaysAgo);
    assertThat(result.lastUpdated()).isEqualTo(response.timing().completeCollection());
  }

  @Test
  void deleteFacilityByIdWithOverlay() {
    Facility f =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    facilityRepository.save(_entityWithOverlay(f, _overlay()));
    Integer status = _controller().deleteFacilityById("vha_f1").getStatusCodeValue();
    assertThat(status).isEqualTo(409);
    assertThat(facilityRepository.findAll()).isEqualTo(List.of(_entityWithOverlay(f, _overlay())));
  }

  @Test
  void deleteFacilityByIdWithoutOverlay() {
    Facility f =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    facilityRepository.save(_entity(f));
    Integer status = _controller().deleteFacilityById("vha_f1").getStatusCodeValue();
    assertThat(status).isEqualTo(200);
    assertThat(facilityRepository.findAll()).isEmpty();
  }

  @Test
  void deleteFacilityOverlayById() {
    Facility f =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    facilityRepository.save(_entityWithOverlay(f, _overlay()));
    Integer status = _controller().deleteCmsOverlayById("vha_f1").getStatusCodeValue();
    assertThat(status).isEqualTo(200);
    assertThat(facilityRepository.findAll()).isEqualTo(List.of(_entityWithOverlay(f, null)));
  }

  @Test
  void deleteFacilityOverlayNotFound() {
    assertThat(_controller().deleteCmsOverlayById("vha_f1").getStatusCodeValue()).isEqualTo(202);
  }

  @Test
  void deleteFromGraveyard_error() {
    FacilityGraveyardRepository repo = mock(FacilityGraveyardRepository.class);
    doThrow(new RuntimeException("oh noez")).when(repo).delete(any(FacilityGraveyardEntity.class));
    InternalFacilitiesController controller =
        InternalFacilitiesController.builder().graveyardRepository(repo).build();
    ReloadResponse response = ReloadResponse.start();
    assertThrows(
        RuntimeException.class,
        () ->
            controller.deleteFromGraveyard(
                response,
                FacilityGraveyardEntity.builder()
                    .id(FacilityEntity.Pk.fromIdString("vha_f1"))
                    .build()));
    assertThat(response.problems())
        .isEqualTo(
            List.of(
                ReloadResponse.Problem.of(
                    "vha_f1", "Failed to delete facility from graveyard: oh noez")));
  }

  @Test
  void deleteNonExistingFacilityByIdReturnsAccepted() {
    assertThat(_controller().deleteFacilityById("vha_f1").getStatusCodeValue()).isEqualTo(202);
  }

  @Test
  @SneakyThrows
  void graveyardAll() {
    Facility f1 = _facility("vha_f1", "NO", "666", 9.0, 9.1, List.of(HealthService.SpecialtyCare));
    CmsOverlay overlay = _overlay();
    FacilityGraveyardEntity entity = _graveyardEntityWithOverlay(f1, overlay);
    graveyardRepository.save(entity);
    assertThat(_controller().graveyardAll())
        .isEqualTo(
            GraveyardResponse.builder()
                .facilities(
                    List.of(
                        GraveyardResponse.Item.builder()
                            .facility(
                                JacksonConfig.createMapper()
                                    .readValue(entity.facility(), Facility.class))
                            .cmsOverlay(overlay)
                            .missing(Instant.ofEpochMilli(entity.missingTimestamp()))
                            .lastUpdated(entity.lastUpdated())
                            .build()))
                .build());
  }

  @Test
  void servicesOf() {
    assertThat(
            InternalFacilitiesController.serviceTypesOf(
                Facility.builder().attributes(FacilityAttributes.builder().build()).build()))
        .isEmpty();
    assertThat(
            InternalFacilitiesController.serviceTypesOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder().services(Services.builder().build()).build())
                    .build()))
        .isEmpty();
    assertThat(
            InternalFacilitiesController.serviceTypesOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .services(
                                Services.builder()
                                    .health(List.of())
                                    .benefits(List.of())
                                    .other(List.of())
                                    .build())
                            .build())
                    .build()))
        .isEmpty();
    assertThat(
            InternalFacilitiesController.serviceTypesOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .services(
                                Services.builder()
                                    .health(
                                        List.of(
                                            HealthService.PrimaryCare,
                                            HealthService.MentalHealthCare))
                                    .benefits(
                                        List.of(
                                            BenefitsService.ApplyingForBenefits,
                                            BenefitsService.BurialClaimAssistance))
                                    .other(List.of(OtherService.OnlineScheduling))
                                    .build())
                            .build())
                    .build()))
        .containsExactlyInAnyOrder(
            HealthService.PrimaryCare,
            HealthService.MentalHealthCare,
            BenefitsService.ApplyingForBenefits,
            BenefitsService.BurialClaimAssistance,
            OtherService.OnlineScheduling);
  }

  @Test
  void stateOf() {
    // No address
    assertThat(
            InternalFacilitiesController.stateOf(
                Facility.builder().attributes(FacilityAttributes.builder().build()).build()))
        .isNull();
    // No physical or mailing
    assertThat(
            InternalFacilitiesController.stateOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder().address(Addresses.builder().build()).build())
                    .build()))
        .isNull();
    // No Physical zip
    assertThat(
            InternalFacilitiesController.stateOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .address(
                                Addresses.builder().physical(Address.builder().build()).build())
                            .build())
                    .build()))
        .isNull();
    // Physical zip
    assertThat(
            InternalFacilitiesController.stateOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .address(
                                Addresses.builder()
                                    .physical(Address.builder().state("FL").build())
                                    .build())
                            .build())
                    .build()))
        .isEqualTo("FL");
  }

  @Test
  void updateAndSave_error() {
    FacilityRepository repo = mock(FacilityRepository.class);
    when(repo.save(any(FacilityEntity.class))).thenThrow(new RuntimeException("oh noez"));
    InternalFacilitiesController controller =
        InternalFacilitiesController.builder().facilityRepository(repo).build();
    Facility f1 =
        _facility("vha_f1", "FL", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    ReloadResponse response = ReloadResponse.start();
    assertThrows(
        RuntimeException.class,
        () ->
            controller.updateAndSave(
                response,
                FacilityEntity.builder().id(FacilityEntity.Pk.fromIdString("vha_f1")).build(),
                f1));
    assertThat(response.problems())
        .isEqualTo(List.of(ReloadResponse.Problem.of("vha_f1", "Failed to save record: oh noez")));
  }

  @Test
  @SneakyThrows
  void upload() {
    Facility f1 =
        _facility("vha_f91", "FU", "South", 1.2, 3.4, List.of(HealthService.MentalHealthCare));
    Facility f2 =
        _facility("vha_f92", "NEAT", "32934", 5.6, 6.7, List.of(HealthService.UrgentCare));
    _controller().upload(List.of(f1, f2));
    assertThat(facilityRepository.findAll()).isEqualTo(List.of(_entity(f1), _entity(f2)));
  }

  @Test
  void zipOf() {
    // No address
    assertThat(
            InternalFacilitiesController.zipOf(
                Facility.builder().attributes(FacilityAttributes.builder().build()).build()))
        .isNull();
    // No physical or mailing
    assertThat(
            InternalFacilitiesController.zipOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder().address(Addresses.builder().build()).build())
                    .build()))
        .isNull();
    // No Physical zip
    assertThat(
            InternalFacilitiesController.zipOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .address(
                                Addresses.builder().physical(Address.builder().build()).build())
                            .build())
                    .build()))
        .isNull();
    // Physical zip
    assertThat(
            InternalFacilitiesController.zipOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .address(
                                Addresses.builder()
                                    .physical(Address.builder().zip("12345").build())
                                    .build())
                            .build())
                    .build()))
        .isEqualTo("12345");
    // Physical zip that is long
    assertThat(
            InternalFacilitiesController.zipOf(
                Facility.builder()
                    .attributes(
                        FacilityAttributes.builder()
                            .address(
                                Addresses.builder()
                                    .physical(Address.builder().zip("12345-9876").build())
                                    .build())
                            .build())
                    .build()))
        .isEqualTo("12345");
  }
}