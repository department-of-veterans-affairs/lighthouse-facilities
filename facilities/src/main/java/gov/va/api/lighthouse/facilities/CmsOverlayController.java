package gov.va.api.lighthouse.facilities;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;

import gov.va.api.lighthouse.facilities.api.cms.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.cms.DetailedService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CmsOverlayController {
  private final FacilityRepository repository;

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @PostMapping(
      value = "/v0/facilities/{id}/cms-overlay",
      produces = "application/json",
      consumes = "application/json")
  @SneakyThrows
  ResponseEntity<Void> saveOverlay(
      @PathVariable("id") String id, @Valid @RequestBody CmsOverlay overlay) {
    Optional<FacilityEntity> existingEntity =
        repository.findById(FacilityEntity.Pk.fromIdString(id));
    if (existingEntity.isEmpty()) {
      log.info("Received Unknown Facility ID ({}) for CMS Overlay", sanitize(id));
      return ResponseEntity.accepted().build();
    }

    FacilityEntity entity = existingEntity.get();

    if (overlay.operatingStatus() != null) {
      entity.cmsOperatingStatus(
          FacilitiesJacksonConfig.createMapper().writeValueAsString(overlay.operatingStatus()));
    }

    if (overlay.detailedServices() != null) {
      // Compile the set of overlay services to be added
      Set<String> detailedServices = new HashSet<>();
      for (DetailedService service : overlay.detailedServices()) {
        // Since the covid 19 service name doesn't match our enum, we need to update and verify??
        if (1 == service.active()) {
          if (service.name().equals("COVID-19 vaccines")) {
            detailedServices.add("Covid19Vaccine");
          } else {
            detailedServices.add(service.name());
          }
        }
      }
      entity.overlayServices(detailedServices);

      // Save the full payload as well
      entity.cmsServices(
          FacilitiesJacksonConfig.createMapper().writeValueAsString(overlay.detailedServices()));
    }

    repository.save(entity);
    return ResponseEntity.ok().build();
  }
}
