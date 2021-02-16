package gov.va.api.lighthouse.facilities.api.v0;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "JSON API-compliant object describing a VA facility")
public final class Facility {
  @Schema(example = "vha_688")
  @NotNull
  String id;

  @NotNull Type type;

  @Valid @NotNull FacilityAttributes attributes;

  public enum ActiveStatus {
    A,
    T
  }

  public enum BenefitsService implements ServiceType {
    ApplyingForBenefits,
    BurialClaimAssistance,
    DisabilityClaimAssistance,
    eBenefitsRegistrationAssistance,
    EducationAndCareerCounseling,
    EducationClaimAssistance,
    FamilyMemberClaimAssistance,
    HomelessAssistance,
    InsuranceClaimAssistanceAndFinancialCounseling,
    IntegratedDisabilityEvaluationSystemAssistance,
    Pensions,
    PreDischargeClaimAssistance,
    TransitionAssistance,
    UpdatingDirectDepositInformation,
    VAHomeLoanAssistance,
    VocationalRehabilitationAndEmploymentAssistance
  }

  public enum FacilityType {
    va_benefits_facility,
    va_cemetery,
    va_health_facility,
    vet_center
  }

  public enum HealthService implements ServiceType {
    Audiology,
    Cardiology,
    Covid19Vaccine,
    DentalServices,
    Dermatology,
    EmergencyCare,
    Gastroenterology,
    Gynecology,
    MentalHealthCare,
    Ophthalmology,
    Optometry,
    Orthopedics,
    Nutrition,
    Podiatry,
    PrimaryCare,
    SpecialtyCare,
    UrgentCare,
    Urology,
    WomensHealth
  }

  public enum OtherService implements ServiceType {
    OnlineScheduling
  }

  public enum Type {
    va_facilities
  }

  public enum OperatingStatusCode {
    NORMAL,
    NOTICE,
    LIMITED,
    CLOSED
  }

  /**
   * This marker interface is used to indicate that an enumeration, such as HealthService, is a type
   * of service offered at a facility.
   */
  public interface ServiceType {}

  // todo descriptions
  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServiceAddress {
    @Schema(example = "50 Irving Street, Northwest")
    @JsonProperty("address_line1")
    String address1;

    @JsonProperty("address_line2")
    String address2;

    @Schema(example = "NY")
    @JsonProperty("administrative_area")
    String administrativeArea;

    @JsonProperty("building_name_number")
    String buildingNameNumber;

    @JsonProperty("clinic_name")
    String clinicName;

    @JsonProperty("country_code")
    String countryCode;

    String locality;

    @Schema(example = "20422-0001")
    @JsonProperty("zip_code")
    String zipCode;

    @Schema(example = "Wing East")
    @JsonProperty("wing_floor_or_room_number")
    String wingFloorOrRoomNumber;
  }

  // todo descriptions
  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServicePhoneNumbers {
    String extension;

    String label;

    String number;

    String type;
  }

  // todo descriptions
  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServiceLocations {

    @JsonProperty("additional_hours_info")
    String additionalHoursInfo;

    @JsonProperty("email_contacts")
    List<DetailedServiceEmailContacts> emailContacts;

    @JsonProperty("facility_service_hours")
    Hours facilityServiceHours;

    @JsonProperty("phone_numbers")
    List<DetailedServicePhoneNumbers> phoneNumbers;

    @JsonProperty("service_location_address")
    DetailedServiceAddress serviceLocationAddress;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServiceEmailContacts {
    @Schema(example = "georgea@va.gov")
    @JsonProperty("email_address")
    String emailAddress;

    @Schema(example = "George Anderson")
    @JsonProperty("email_label")
    String emailLabel;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Address {
    @Schema(example = "50 Irving Street, Northwest")
    @JsonProperty("address_1")
    String address1;

    @JsonProperty("address_2")
    String address2;

    @JsonProperty("address_3")
    String address3;

    @Schema(example = "20422-0001")
    String zip;

    @Schema(example = "Washington")
    String city;

    @Schema(example = "DC")
    String state;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Addresses {
    @Valid Address mailing;

    @Valid Address physical;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonPropertyOrder({
    "name",
    "facility_type",
    "classification",
    "website",
    "lat",
    "long",
    "address",
    "phone",
    "hours",
    "operational_hours_special_instructions",
    "services",
    "satisfaction",
    "wait_times",
    "mobile",
    "active_status",
    "operating_status",
    "detailed_services",
    "visn"
  })
  public static final class FacilityAttributes {
    @Schema(example = "Washington VA Medical Center")
    @NotNull
    String name;

    @NotNull
    @JsonProperty("facility_type")
    FacilityType facilityType;

    @Schema(example = "VA Medical Center (VAMC)")
    String classification;

    @Schema(example = "http://www.washingtondc.va.gov")
    String website;

    @NotNull
    @Schema(description = "Facility latitude", format = "float", example = "38.9311137")
    @JsonProperty("lat")
    BigDecimal latitude;

    @NotNull
    @Schema(description = "Facility longitude", format = "float", example = "-77.0109110499999")
    @JsonProperty("long")
    BigDecimal longitude;

    @Valid Addresses address;

    @Valid Phone phone;

    @Valid Hours hours;

    @Schema(example = "Administrative hours are Monday-Friday 8:00 a.m. to 4:30 p.m.")
    @JsonProperty("operational_hours_special_instructions")
    String operationalHoursSpecialInstructions;

    @Valid Services services;

    @Valid Satisfaction satisfaction;

    @Valid
    @JsonProperty("wait_times")
    WaitTimes waitTimes;

    @Schema(example = "false")
    Boolean mobile;

    @JsonProperty("active_status")
    @Schema(description = "This field is deprecated and replaced with \"operating_status\".")
    ActiveStatus activeStatus;

    @Valid
    @NotNull
    @JsonProperty(value = "operating_status", required = true)
    OperatingStatus operatingStatus;

    @Valid
    @JsonProperty(value = "detailed_services", required = true)
    List<CmsService> cmsServices;

    @Schema(example = "20")
    String visn;

    public static final class FacilityAttributesBuilder {
      @JsonProperty("operationalHoursSpecialInstructions")
      public FacilityAttributesBuilder instructions(String val) {
        return operationalHoursSpecialInstructions(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.")
  public static final class Hours {
    @Schema(example = "9AM-5PM")
    String monday;

    @Schema(example = "9AM-5PM")
    String tuesday;

    @Schema(example = "9AM-5PM")
    String wednesday;

    @Schema(example = "9AM-5PM")
    String thursday;

    @Schema(example = "9AM-5PM")
    String friday;

    @Schema(example = "Closed")
    String saturday;

    @Schema(example = "Closed")
    String sunday;

    public static final class HoursBuilder {
      @JsonProperty("Friday")
      public HoursBuilder fri(String val) {
        return friday(val);
      }

      @JsonProperty("Monday")
      public HoursBuilder mon(String val) {
        return monday(val);
      }

      @JsonProperty("Saturday")
      public HoursBuilder sat(String val) {
        return saturday(val);
      }

      @JsonProperty("Sunday")
      public HoursBuilder sun(String val) {
        return sunday(val);
      }

      @JsonProperty("Thursday")
      public HoursBuilder thurs(String val) {
        return thursday(val);
      }

      @JsonProperty("Tuesday")
      public HoursBuilder tues(String val) {
        return tuesday(val);
      }

      @JsonProperty("Wednesday")
      public HoursBuilder wed(String val) {
        return wednesday(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(
      description =
          "Current status of facility operations."
              + " The overall status of the facility, which can be:"
              + " Normal Hours and Services,"
              + " Facility Notice,"
              + " Limited Hours and/or Services,"
              + " or Closed."
              + " This field replaces active_status.")
  public static final class OperatingStatus {
    @NotNull
    @JsonProperty(required = true)
    @Schema(
        example = "NORMAL",
        description =
            "Status codes indicate normal hours/services,"
                + " limited hours/services, closed operations,"
                + " or published facility notices for visitors.")
    OperatingStatusCode code;

    @JsonProperty(value = "additional_info", required = false)
    @Size(max = 300)
    @Schema(
        description =
            "Details of facility notices for visitors,"
                + " such as messages about parking lot closures or"
                + " floor visitation information.")
    String additionalInfo;
  }

  @Data
  @Builder
  @JsonInclude()
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(
      description =
          "Current descriptions of facility service via CMS."
              + "The descriptions of the facility service can be: "
              + " Active/Inactive,"
              + " National Description,"
              + " System Description,"
              + " Facility Description,"
              + " or Health Service API ID.")
  public static final class CmsService {
    @Schema(example = "COVID-19 vaccines")
    String name;

    @Schema(
        example = "0",
        type = "integer",
        description = "0 means service is inactive and 1 means active")
    int active;

    // todo add description
    @Schema(example = "2021-02-04T22:36:49+00:00", description = "todo")
    String changed;

    @Schema(example = "Vaccine availability for COVID-19")
    @JsonProperty("description_national")
    String descriptionNational;

    @Schema(example = "System description for vaccine availability for COVID-19")
    @JsonProperty("description_system")
    String descriptionSystem;

    @Schema(example = "Facility description for vaccine availability for COVID-19")
    @JsonProperty("description_facility")
    String descriptionFacility;

    @Schema(example = "12345")
    @JsonProperty("health_service_api_id")
    String healthServiceApiId;

    // todo example?
    @Schema(example = "Your VA health care team will contact you if you...more text")
    @JsonProperty("hservice_appt_leadin")
    String healthServiceAppointmentLeadIn;

    @Schema(
        example = "0",
        type = "integer",
        description = "0 means online scheduling is unavailable and 1 means available")
    @JsonProperty("online_scheduling_available")
    int onlineSchedulingAvailable;

    // todo example?
    @Schema(
        example = "\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines")
    String path;

    // todo description and example?
    @JsonProperty("phone_numbers")
    List<DetailedServicePhoneNumbers> phoneNumbers;

    @Schema(
        example = "0",
        type = "integer",
        description = "0 means referral is not required and 1 means required")
    @JsonProperty("referral_required")
    int referralRequired;

    // todo description and example?
    @JsonProperty("service_locations")
    List<DetailedServiceLocations> serviceLocations;

    @Schema(
        example = "1",
        type = "integer",
        description = "0 means walk ins are not accepted and 1 means accepted")
    @JsonProperty("walk_ins_accepted")
    int walkInsAccepted;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(description = "Veteran-reported satisfaction scores for health care services")
  public static final class PatientSatisfaction {
    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a primary care location.")
    @JsonProperty("primary_care_urgent")
    BigDecimal primaryCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a primary care location.")
    @JsonProperty("primary_care_routine")
    BigDecimal primaryCareRoutine;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a specialty location.")
    @JsonProperty("specialty_care_urgent")
    BigDecimal specialtyCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a specialty location.")
    @JsonProperty("specialty_care_routine")
    BigDecimal specialtyCareRoutine;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(
      description =
          "Expected wait times for new and established patients for a given health care service")
  public static final class PatientWaitTime {
    @NotNull HealthService service;

    @Schema(
        example = "10",
        description =
            "Average number of days a Veteran who hasn't been to this location has to wait "
                + "for a non-urgent appointment.")
    @JsonProperty("new")
    BigDecimal newPatientWaitTime;

    @Schema(
        example = "5",
        description =
            "Average number of days a patient who has already been to this location has to wait "
                + "for a non-urgent appointment.")
    @JsonProperty("established")
    BigDecimal establishedPatientWaitTime;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Phone {
    @Schema(example = "202-555-1212")
    String fax;

    @Schema(example = "202-555-1212")
    String main;

    @Schema(example = "202-555-1212")
    String pharmacy;

    @Schema(example = "202-555-1212")
    @JsonProperty("after_hours")
    String afterHours;

    @Schema(example = "202-555-1212")
    @JsonProperty("patient_advocate")
    String patientAdvocate;

    @Schema(example = "202-555-1212")
    @JsonProperty("mental_health_clinic")
    String mentalHealthClinic;

    @Schema(example = "202-555-1212")
    @JsonProperty("enrollment_coordinator")
    String enrollmentCoordinator;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Satisfaction {
    @Valid PatientSatisfaction health;

    @Schema(example = "2018-01-01")
    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class Services {
    List<OtherService> other;

    List<HealthService> health;

    List<BenefitsService> benefits;

    @Schema(example = "2018-01-01")
    @JsonProperty("last_updated")
    LocalDate lastUpdated;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class WaitTimes {
    @Valid List<PatientWaitTime> health;

    @Schema(example = "2018-01-01")
    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }
}
