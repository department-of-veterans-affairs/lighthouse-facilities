package gov.va.api.lighthouse.facilities.api.cms;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude()
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonPropertyOrder({
  "name",
  "description_facility",
  "appt_leadin",
  "phone_numbers",
  "online_scheduling_available",
  "referral_required",
  "walk_ins_accepted",
  "service_locations"
})
@Schema(description = "Detailed information of a facility service")
public class DetailedService {
  @Schema(example = "COVID-19 vaccines")
  String name;

  @JsonIgnore
  @Schema(
      example = "0",
      type = "integer",
      description = "0 means service is inactive and 1 means active")
  int active;

  // todo add description
  @JsonIgnore
  @Schema(example = "2021-02-04T22:36:49+00:00", description = "todo")
  String changed;

  @JsonIgnore
  @Schema(example = "Vaccine availability for COVID-19")
  @JsonProperty("description_national")
  String descriptionNational;

  @JsonIgnore
  @Schema(example = "System description for vaccine availability for COVID-19")
  @JsonProperty("description_system")
  String descriptionSystem;

  @Schema(example = "Facility description for vaccine availability for COVID-19")
  @JsonProperty("description_facility")
  String descriptionFacility;

  @JsonIgnore
  @Schema(example = "12345")
  @JsonProperty("health_service_api_id")
  String healthServiceApiId;

  // todo example?
  @Schema(example = "Your VA health care team will contact you if you...more text")
  @JsonProperty("appt_leadin")
  String appointmentLeadIn;

  @Schema(
      example = "0",
      type = "integer",
      description = "0 means online scheduling is unavailable and 1 means available")
  @JsonProperty("online_scheduling_available")
  int onlineSchedulingAvailable;

  // todo example?
  @JsonIgnore
  @Schema(example = "\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines")
  String path;

  // todo description and example?
  @JsonProperty("phone_numbers")
  List<DetailedServicePhoneNumber> phoneNumbers;

  @Schema(
      example = "0",
      type = "integer",
      description = "0 means referral is not required and 1 means required")
  @JsonProperty("referral_required")
  int referralRequired;

  // todo description and example?
  @JsonProperty("service_locations")
  List<DetailedServiceLocation> serviceLocations;

  @Schema(
      example = "1",
      type = "integer",
      description = "0 means walk ins are not accepted and 1 means accepted")
  @JsonProperty("walk_ins_accepted")
  int walkInsAccepted;

  // todo descriptions
  @Data
  @Builder
  @JsonInclude()
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonPropertyOrder({
    "building_name_number",
    "clinic_name",
    "wing_floor_or_room_number",
    "address_line1",
    "address_line2",
    "city",
    "state",
    "zip_code",
    "country_code"
  })
  public static final class DetailedServiceAddress {
    @Schema(example = "50 Irving Street, Northwest")
    @JsonProperty("address_line1")
    String address1;

    @JsonProperty("address_line2")
    String address2;

    // todo state or admin area?
    @Schema(example = "NY")
    @JsonProperty("state")
    String state;

    @JsonProperty("building_name_number")
    String buildingNameNumber;

    @JsonProperty("clinic_name")
    String clinicName;

    @JsonProperty("country_code")
    String countryCode;

    // todo is this city or locality? waiting on confirmation
    String city;

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
  @JsonInclude()
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServicePhoneNumber {
    String extension;

    String label;

    String number;

    String type;
  }

  // todo descriptions
  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonPropertyOrder({
    "service_location_address",
    "phone_numbers",
    "email_contacts",
    "facility_service_hours",
    "additional_hours_info"
  })
  public static final class DetailedServiceLocation {

    @JsonProperty("additional_hours_info")
    String additionalHoursInfo;

    @JsonProperty("email_contacts")
    List<DetailedServiceEmailContact> emailContacts;

    @JsonProperty("facility_service_hours")
    @Valid
    DetailedServiceHours facilityServiceHours;

    @JsonProperty("phone_numbers")
    List<DetailedServicePhoneNumber> phoneNumbers;

    @JsonProperty("service_location_address")
    DetailedServiceAddress serviceLocationAddress;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServiceEmailContact {
    @Schema(example = "georgea@va.gov")
    @JsonProperty("email_address")
    String emailAddress;

    @Schema(example = "George Anderson")
    @JsonProperty("mail_to")
    String mailTo;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.")
  public static final class DetailedServiceHours {
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
  }
}
