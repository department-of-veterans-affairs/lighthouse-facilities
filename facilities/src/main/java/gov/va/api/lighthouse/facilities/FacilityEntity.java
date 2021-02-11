package gov.va.api.lighthouse.facilities;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toSet;

import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import lombok.NonNull;
import org.springframework.data.domain.Sort;

@Data
@Entity
@Builder
@Table(name = "facility", schema = "app")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FacilityEntity implements HasFacilityPayload {
  /**
   * API V0 searches by {type}_{facilityId}. We might want to change that in the future, so we are
   * keeping those two pieces of information separate. However, the two (type + facility) are
   * unique, since it is possible for a facility to have multiple types, e.g. benefits and health.
   */
  @EqualsAndHashCode.Include @EmbeddedId private Pk id;

  @Column(length = 5)
  private String zip;

  /** States are two letter abbreviations, except when they are jacked up and say "South". */
  @Column(length = 5)
  private String state;

  @Column private double latitude;

  @Column private double longitude;

  @ElementCollection(targetClass = String.class)
  @CollectionTable(
      name = "facility_services",
      schema = "app",
      joinColumns = {@JoinColumn(name = "station_number"), @JoinColumn(name = "type")})
  @Column(length = 48)
  private Set<String> services;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column
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
      name = "cms_overlay_detailed_services",
      schema = "app",
      joinColumns = {@JoinColumn(name = "station_number"), @JoinColumn(name = "type")})
  @Column(length = 48, name = "overlay_detailed_services")
  private Set<String> overlayServices;

  @Version private Integer version;

  @Column(name = "missing_timestamp")
  private Long missingTimestamp;

  @Column(name = "last_updated")
  private Instant lastUpdated;

  @Column(name = "VISN")
  private String visn;

  @Column(name = "mobile")
  private Boolean mobile;

  /** Builder alternative that allows enums to be specified instead of strings. */
  @Builder(
      builderMethodName = "typeSafeBuilder",
      builderClassName = "FacilityEntityTypeSafeBuilder")
  public FacilityEntity(
      Pk id,
      String zip,
      String state,
      double latitude,
      double longitude,
      String facility,
      String cmsOperatingStatus,
      String cmsServices,
      Set<Facility.ServiceType> overlayServiceTypes,
      Integer version,
      Set<Facility.ServiceType> servicesTypes,
      Long missingTimestamp,
      Instant lastUpdated,
      String visn,
      Boolean mobile) {
    this(
        id,
        zip,
        state,
        latitude,
        longitude,
        servicesTypes.stream().map(Object::toString).collect(toSet()),
        facility,
        cmsOperatingStatus,
        cmsServices,
        overlayServiceTypes.stream().map(Object::toString).collect(toSet()),
        version,
        missingTimestamp,
        lastUpdated,
        visn,
        mobile);
  }

  static Sort naturalOrder() {
    return Sort.by("id").ascending();
  }

  /** Populate overlay services from a type safe collection. */
  public void overlayServicesFromServiceTypes(Set<Facility.ServiceType> overlayServiceTypes) {
    overlayServices(overlayServiceTypes.stream().map(Object::toString).collect(toSet()));
  }

  /** Populate services from a type safe collection. */
  public void servicesFromServiceTypes(Set<Facility.ServiceType> serviceTypes) {
    services(serviceTypes.stream().map(Object::toString).collect(toSet()));
  }

  public enum Type {
    /** Health facility. */
    vha,
    /** Benefits facility. */
    vba,
    /** Cemetery. */
    nca,
    /** Vet Center. */
    vc
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor(staticName = "of")
  @Embeddable
  static final class Pk implements Serializable {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "station_number", nullable = false)
    private String stationNumber;

    /** Create a Pk from the {type}_{id} style used in the Facilities API ID. */
    static Pk fromIdString(@NonNull String typeAndStationNumber) {
      int separator = typeAndStationNumber.indexOf('_');
      checkArgument(separator > 0, typeAndStationNumber);
      checkArgument(separator + 1 < typeAndStationNumber.length() - 1, typeAndStationNumber);
      String typeValue = typeAndStationNumber.substring(0, separator);
      String stationNumber = typeAndStationNumber.substring(separator + 1);
      checkArgument(!stationNumber.isBlank(), typeAndStationNumber);
      return of(Type.valueOf(typeValue), stationNumber);
    }

    /**
     * Create a Pk from the {type}_{id} style used in the Facilities API ID, suppressing exceptions.
     */
    static Optional<Pk> optionalFromIdString(@NonNull String typeAndStationNumber) {
      try {
        return Optional.ofNullable(fromIdString(typeAndStationNumber));
      } catch (Exception ex) {
        return Optional.empty();
      }
    }

    public String toIdString() {
      return type + "_" + stationNumber;
    }
  }
}
