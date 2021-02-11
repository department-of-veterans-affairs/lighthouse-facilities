package gov.va.api.lighthouse.facilities;

import java.util.Set;

/** A DTO projection of the FacilityEntity. */
public interface HasFacilityPayload {
  String cmsOperatingStatus();

  String cmsServices();

  String facility();

  Set<String> overlayServices();
}
