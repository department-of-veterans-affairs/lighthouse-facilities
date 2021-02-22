package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.collector.Transformers.allBlank;
import static gov.va.api.lighthouse.facilities.collector.Transformers.checkAngleBracketNull;
import static gov.va.api.lighthouse.facilities.collector.Transformers.phoneTrim;
import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.util.Locale;
import lombok.Builder;
import lombok.NonNull;

@Builder
final class CemeteriesTransformer {
  @NonNull CdwCemetery cdwFacility;

  String facilityName;

  String website;

  private Facility.FacilityAttributes attributes() {
    return Facility.FacilityAttributes.builder()
        .name(facilityName(cdwFacility.fullName()))
        .facilityType(Facility.FacilityType.va_cemetery)
        .classification(cdwFacility.siteType())
        .latitude(cdwFacility.latitude())
        .longitude(cdwFacility.longitude())
        .website(website(cdwFacility.websiteUrl()))
        .address(
            Facility.Addresses.builder()
                .physical(
                    Facility.Address.builder()
                        .address1(checkAngleBracketNull(cdwFacility.siteAddress1()))
                        .address2(checkAngleBracketNull(cdwFacility.siteAddress2()))
                        .city(cdwFacility.siteCity())
                        .state(upperCase(cdwFacility.siteState(), Locale.US))
                        .zip(cdwFacility.siteZip())
                        .build())
                .mailing(
                    Facility.Address.builder()
                        .address1(checkAngleBracketNull(cdwFacility.mailAddress1()))
                        .address2(checkAngleBracketNull(cdwFacility.mailAddress2()))
                        .city(cdwFacility.mailCity())
                        .state(upperCase(cdwFacility.mailState(), Locale.US))
                        .zip(cdwFacility.mailZip())
                        .build())
                .build())
        .phone(phone(cdwFacility.phone(), cdwFacility.fax()))
        .hours(
            Facility.Hours.builder()
                .monday(cdwFacility.visitationHoursWeekday())
                .tuesday(cdwFacility.visitationHoursWeekday())
                .wednesday(cdwFacility.visitationHoursWeekday())
                .thursday(cdwFacility.visitationHoursWeekday())
                .friday(cdwFacility.visitationHoursWeekday())
                .saturday(cdwFacility.visitationHoursWeekend())
                .sunday(cdwFacility.visitationHoursWeekend())
                .build())
        .build();
  }

  String facilityName(String cdwName) {
    return facilityName != null ? facilityName : cdwName;
  }

  private Facility.Phone phone(String attPhone, String attFax) {
    String main = phoneTrim(attPhone);
    String fax = phoneTrim(attFax);
    if (allBlank(main, fax)) {
      return null;
    } else {
      return Facility.Phone.builder().main(main).fax(fax).build();
    }
  }

  Facility toFacility() {
    return Facility.builder()
        .id("nca_" + cdwFacility.siteId())
        .type(Facility.Type.va_facilities)
        .attributes(attributes())
        .build();
  }

  String website(String cdwUrl) {
    return website != null ? website : cdwUrl;
  }
}
