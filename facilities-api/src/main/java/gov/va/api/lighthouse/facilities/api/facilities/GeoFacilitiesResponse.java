package gov.va.api.lighthouse.facilities.api.facilities;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoFacilitiesResponse {
  @NotNull Type type;

  @Valid List<GeoFacility> features;

  public enum Type {
    FeatureCollection
  }
}