package gov.va.api.lighthouse.facilities;

import java.time.Instant;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "facility_graveyard", schema = "app")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FacilityGraveyardEntity {
  @EqualsAndHashCode.Include @EmbeddedId private FacilityEntity.Pk id;

  @Version private Integer version;

  @Lob
  @Column
  @Basic(fetch = FetchType.EAGER)
  private String facility;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "cms_operating_status")
  private String cmsOperatingStatus;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "cms_services")
  private String cmsServices;

  @ElementCollection(targetClass = String.class)
  @CollectionTable(
      name = "cms_graveyard_overlay_detailed_services",
      schema = "app",
      joinColumns = {@JoinColumn(name = "station_number"), @JoinColumn(name = "type")})
  @Column(length = 48, name = "graveyard_overlay_detailed_services")
  private Set<String> graveyardOverlayServices;

  @Column(name = "missing_timestamp")
  private Long missingTimestamp;

  @Column(name = "last_updated")
  private Instant lastUpdated;
}
