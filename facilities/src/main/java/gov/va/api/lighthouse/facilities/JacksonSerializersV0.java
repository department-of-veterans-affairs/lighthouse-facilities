package gov.va.api.lighthouse.facilities;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import gov.va.api.lighthouse.facilities.api.v0.FacilitiesResponse;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.FacilityReadResponse;
import gov.va.api.lighthouse.facilities.api.v0.GeoFacility;
import gov.va.api.lighthouse.facilities.api.v0.GeoFacilityReadResponse;
import gov.va.api.lighthouse.facilities.api.v0.NearbyResponse;
import gov.va.api.lighthouse.facilities.api.v0.PageLinks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
final class JacksonSerializersV0 {
  private static boolean hasParent(JsonGenerator jgen, Class<?> clazz) {
    return parents(jgen).stream().anyMatch(p -> clazz.isInstance(p));
  }

  private static Optional<String> id(Object obj) {
    if (obj instanceof Facility) {
      return Optional.ofNullable(((Facility) obj).id());
    }
    if (obj instanceof GeoFacility.Properties) {
      return Optional.ofNullable(((GeoFacility.Properties) obj).id());
    }
    if (obj instanceof GeoFacility) {
      return Optional.ofNullable(((GeoFacility) obj).properties()).map(x -> x.id());
    }
    if (obj instanceof FacilityReadResponse) {
      return Optional.ofNullable(((FacilityReadResponse) obj).facility()).map(x -> x.id());
    }
    if (obj instanceof GeoFacilityReadResponse) {
      return Optional.ofNullable(((GeoFacilityReadResponse) obj).properties()).map(x -> x.id());
    }
    if (obj instanceof NearbyResponse.Nearby) {
      return Optional.ofNullable(((NearbyResponse.Nearby) obj).id());
    }
    return Optional.empty();
  }

  private static boolean idStartsWith(JsonGenerator jgen, String prefix) {
    return parents(jgen).stream()
        .map(p -> id(p))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .anyMatch(id -> StringUtils.startsWithIgnoreCase(id, prefix));
  }

  private static List<Object> parents(JsonGenerator jgen) {
    List<Object> parents = new ArrayList<>();
    JsonStreamContext parent = jgen.getOutputContext().getParent();
    while (parent != null) {
      if (parent.getCurrentValue() != null) {
        parents.add(parent.getCurrentValue());
      }
      parent = parent.getParent();
    }
    return parents;
  }

  /** Custom serialization rules for V0 API classes. */
  static SimpleModule serializersV0() {
    SimpleModule mod = new SimpleModule();
    mod.addSerializer(Facility.Address.class, new AddressSerializer());
    mod.addSerializer(Facility.Addresses.class, new AddressesSerializer());
    mod.addSerializer(
        FacilitiesResponse.FacilitiesMetadata.class, new FacilitiesMetadataSerializer());
    mod.addSerializer(Facility.FacilityAttributes.class, new FacilityAttributesSerializer());
    mod.addSerializer(Facility.Hours.class, new HoursSerializer());
    mod.addSerializer(Facility.PatientWaitTime.class, new PatientWaitTimeSerializer());
    mod.addSerializer(Facility.Phone.class, new PhoneSerializer());
    mod.addSerializer(Facility.Satisfaction.class, new SatisfactionSerializer());
    mod.addSerializer(Facility.Services.class, new ServicesSerializer());
    mod.addSerializer(Facility.WaitTimes.class, new WaitTimesSerializer());
    mod.addSerializer(GeoFacility.Properties.class, new PropertiesSerializer());
    mod.addSerializer(PageLinks.class, new PageLinksSerializer());
    return mod;
  }

  @SneakyThrows
  private static void writeNonNull(JsonGenerator jgen, String fieldName, Object value) {
    if (value != null) {
      jgen.writeObjectField(fieldName, value);
    }
  }

  private static final class AddressSerializer extends StdSerializer<Facility.Address> {
    public AddressSerializer() {
      this(null);
    }

    public AddressSerializer(Class<Facility.Address> t) {
      super(t);
    }

    private static boolean empty(Facility.Address value) {
      return value.zip() == null
          && value.city() == null
          && value.state() == null
          && value.address1() == null
          && value.address2() == null
          && value.address3() == null;
    }

    private static boolean emptyExcludeAddr1(Facility.Address value) {
      return value.zip() == null
          && value.city() == null
          && value.state() == null
          && value.address2() == null
          && value.address3() == null;
    }

    @SneakyThrows
    private static void writeNonNullOrNonStateCem(
        JsonGenerator jgen, String fieldName, String value) {
      if (value != null || !idStartsWith(jgen, "nca_s")) {
        jgen.writeStringField(fieldName, value);
      }
    }

    @Override
    @SneakyThrows
    public void serialize(Facility.Address value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value)) {
        if (emptyExcludeAddr1(value) && idStartsWith(jgen, "nca_s")) {
          jgen.writeStringField("address_1", value.address1());
        } else {
          jgen.writeStringField("zip", value.zip());
          writeNonNullOrNonStateCem(jgen, "city", value.city());
          writeNonNullOrNonStateCem(jgen, "state", value.state());
          writeNonNullOrNonStateCem(jgen, "address_1", value.address1());
          writeNonNullOrNonStateCem(jgen, "address_2", value.address2());
          writeNonNullOrNonStateCem(jgen, "address_3", value.address3());
        }
      } else if (idStartsWith(jgen, "nca_") && !idStartsWith(jgen, "nca_s")) {
        jgen.writeStringField("zip", null);
        jgen.writeStringField("city", null);
        jgen.writeStringField("state", null);
        jgen.writeStringField("address_1", null);
        jgen.writeStringField("address_2", null);
        jgen.writeStringField("address_3", null);
      }
      jgen.writeEndObject();
    }
  }

  private static final class AddressesSerializer extends StdSerializer<Facility.Addresses> {
    public AddressesSerializer() {
      this(null);
    }

    public AddressesSerializer(Class<Facility.Addresses> t) {
      super(t);
    }

    private static boolean empty(Facility.Addresses value) {
      return value.mailing() == null && value.physical() == null;
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.Addresses value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value)) {
        jgen.writeObjectField(
            "mailing",
            Optional.ofNullable(value.mailing()).orElse(Facility.Address.builder().build()));
        jgen.writeObjectField(
            "physical",
            Optional.ofNullable(value.physical()).orElse(Facility.Address.builder().build()));
      }
      jgen.writeEndObject();
    }
  }

  private static final class FacilitiesMetadataSerializer
      extends StdSerializer<FacilitiesResponse.FacilitiesMetadata> {
    public FacilitiesMetadataSerializer() {
      this(null);
    }

    public FacilitiesMetadataSerializer(Class<FacilitiesResponse.FacilitiesMetadata> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(
        FacilitiesResponse.FacilitiesMetadata value,
        JsonGenerator jgen,
        SerializerProvider provider) {
      jgen.writeStartObject();
      jgen.writeObjectField("pagination", value.pagination());
      jgen.writeObjectField(
          "distances", Optional.ofNullable(value.distances()).orElse(emptyList()));
      jgen.writeEndObject();
    }
  }

  private static final class FacilityAttributesSerializer
      extends StdSerializer<Facility.FacilityAttributes> {
    public FacilityAttributesSerializer() {
      this(null);
    }

    public FacilityAttributesSerializer(Class<Facility.FacilityAttributes> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.FacilityAttributes value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      jgen.writeObjectField("name", value.name());
      jgen.writeObjectField("facility_type", value.facilityType());
      jgen.writeObjectField("classification", value.classification());
      jgen.writeObjectField("website", value.website());
      jgen.writeObjectField("lat", value.latitude());
      jgen.writeObjectField("long", value.longitude());
      jgen.writeObjectField(
          "address",
          Optional.ofNullable(value.address()).orElse(Facility.Addresses.builder().build()));
      jgen.writeObjectField(
          "phone", Optional.ofNullable(value.phone()).orElse(Facility.Phone.builder().build()));
      jgen.writeObjectField(
          "hours", Optional.ofNullable(value.hours()).orElse(Facility.Hours.builder().build()));
      jgen.writeObjectField(
          "operational_hours_special_instructions", value.operationalHoursSpecialInstructions());
      jgen.writeObjectField(
          "services",
          Optional.ofNullable(value.services()).orElse(Facility.Services.builder().build()));
      jgen.writeObjectField(
          "satisfaction",
          Optional.ofNullable(value.satisfaction())
              .orElse(Facility.Satisfaction.builder().build()));
      jgen.writeObjectField(
          "wait_times",
          Optional.ofNullable(value.waitTimes()).orElse(Facility.WaitTimes.builder().build()));
      jgen.writeObjectField("mobile", value.mobile());
      jgen.writeObjectField("active_status", value.activeStatus());
      jgen.writeObjectField("operating_status", value.operatingStatus());
      jgen.writeObjectField("detailed_services", value.detailedServices());
      jgen.writeObjectField("visn", value.visn());
      jgen.writeEndObject();
    }
  }

  private static final class HoursSerializer extends StdSerializer<Facility.Hours> {
    public HoursSerializer() {
      this(null);
    }

    public HoursSerializer(Class<Facility.Hours> t) {
      super(t);
    }

    private static boolean empty(Facility.Hours value) {
      return value.monday() == null
          && value.tuesday() == null
          && value.wednesday() == null
          && value.thursday() == null
          && value.friday() == null
          && value.saturday() == null
          && value.sunday() == null;
    }

    private static void writeLower(Facility.Hours value, JsonGenerator jgen) {
      writeNonNull(jgen, "monday", value.monday());
      writeNonNull(jgen, "tuesday", value.tuesday());
      writeNonNull(jgen, "wednesday", value.wednesday());
      writeNonNull(jgen, "thursday", value.thursday());
      writeNonNull(jgen, "friday", value.friday());
      writeNonNull(jgen, "saturday", value.saturday());
      writeNonNull(jgen, "sunday", value.sunday());
    }

    private static void writeLowerByLength(Facility.Hours value, JsonGenerator jgen) {
      writeNonNull(jgen, "friday", value.friday());
      writeNonNull(jgen, "monday", value.monday());
      writeNonNull(jgen, "sunday", value.sunday());
      writeNonNull(jgen, "tuesday", value.tuesday());
      writeNonNull(jgen, "saturday", value.saturday());
      writeNonNull(jgen, "thursday", value.thursday());
      writeNonNull(jgen, "wednesday", value.wednesday());
    }

    private static void writeUpper(Facility.Hours value, JsonGenerator jgen) {
      writeNonNull(jgen, "Monday", value.monday());
      writeNonNull(jgen, "Tuesday", value.tuesday());
      writeNonNull(jgen, "Wednesday", value.wednesday());
      writeNonNull(jgen, "Thursday", value.thursday());
      writeNonNull(jgen, "Friday", value.friday());
      writeNonNull(jgen, "Saturday", value.saturday());
      writeNonNull(jgen, "Sunday", value.sunday());
    }

    private static void writeUpperByLength(Facility.Hours value, JsonGenerator jgen) {
      writeNonNull(jgen, "Friday", value.friday());
      writeNonNull(jgen, "Monday", value.monday());
      writeNonNull(jgen, "Sunday", value.sunday());
      writeNonNull(jgen, "Tuesday", value.tuesday());
      writeNonNull(jgen, "Saturday", value.saturday());
      writeNonNull(jgen, "Thursday", value.thursday());
      writeNonNull(jgen, "Wednesday", value.wednesday());
    }

    @Override
    @SneakyThrows
    public void serialize(Facility.Hours value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value)) {
        if (hasParent(jgen, FacilityReadResponse.class)) {
          writeLower(value, jgen);
        } else if (hasParent(jgen, Facility.class)) {
          writeLowerByLength(value, jgen);
        } else if (hasParent(jgen, GeoFacilityReadResponse.class)) {
          if (idStartsWith(jgen, "vc_")) {
            writeLower(value, jgen);
          } else {
            writeUpper(value, jgen);
          }
        } else if (hasParent(jgen, GeoFacility.class)) {
          if (idStartsWith(jgen, "vc_")) {
            writeLowerByLength(value, jgen);
          } else {
            writeUpperByLength(value, jgen);
          }
        }
      } else if (idStartsWith(jgen, "nca_")) {
        jgen.writeObjectField("Friday", null);
        jgen.writeObjectField("Monday", null);
        jgen.writeObjectField("Sunday", null);
        jgen.writeObjectField("Tuesday", null);
        jgen.writeObjectField("Saturday", null);
        jgen.writeObjectField("Thursday", null);
        jgen.writeObjectField("Wednesday", null);
      }
      jgen.writeEndObject();
    }
  }

  private static final class PageLinksSerializer extends StdSerializer<PageLinks> {
    public PageLinksSerializer() {
      this(null);
    }

    public PageLinksSerializer(Class<PageLinks> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(PageLinks value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (hasParent(jgen, NearbyResponse.class)) {
        jgen.writeObjectField("related", value.related());
      }
      jgen.writeObjectField("self", value.self());
      jgen.writeObjectField("first", value.first());
      jgen.writeObjectField("prev", value.prev());
      jgen.writeObjectField("next", value.next());
      jgen.writeObjectField("last", value.last());
      jgen.writeEndObject();
    }
  }

  private static final class PatientWaitTimeSerializer
      extends StdSerializer<Facility.PatientWaitTime> {
    public PatientWaitTimeSerializer() {
      this(null);
    }

    public PatientWaitTimeSerializer(Class<Facility.PatientWaitTime> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.PatientWaitTime value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      jgen.writeObjectField("service", value.service());
      jgen.writeObjectField("new", value.newPatientWaitTime());
      jgen.writeObjectField("established", value.establishedPatientWaitTime());
      jgen.writeEndObject();
    }
  }

  private static final class PhoneSerializer extends StdSerializer<Facility.Phone> {
    public PhoneSerializer() {
      this(null);
    }

    public PhoneSerializer(Class<Facility.Phone> t) {
      super(t);
    }

    private static boolean empty(Facility.Phone value) {
      return value.fax() == null
          && value.main() == null
          && value.pharmacy() == null
          && value.afterHours() == null
          && value.patientAdvocate() == null
          && value.mentalHealthClinic() == null
          && value.enrollmentCoordinator() == null;
    }

    @SneakyThrows
    private static void writeNonNullOrBenOrHealthOrStateCem(
        JsonGenerator jgen, String fieldName, String value) {
      if (value != null
          || idStartsWith(jgen, "vba_")
          || idStartsWith(jgen, "vha_")
          || idStartsWith(jgen, "nca_s")) {
        jgen.writeObjectField(fieldName, value);
      }
    }

    @SneakyThrows
    private static void writeNonNullOrHealth(JsonGenerator jgen, String fieldName, String value) {
      if (value != null || idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField(fieldName, value);
      }
    }

    @Override
    @SneakyThrows
    public void serialize(Facility.Phone value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value)) {
        writeNonNullOrBenOrHealthOrStateCem(jgen, "fax", value.fax());
        jgen.writeObjectField("main", value.main());
        writeNonNullOrHealth(jgen, "pharmacy", value.pharmacy());
        writeNonNullOrHealth(jgen, "after_hours", value.afterHours());
        writeNonNullOrHealth(jgen, "patient_advocate", value.patientAdvocate());
        writeNonNull(jgen, "mental_health_clinic", value.mentalHealthClinic());
        writeNonNullOrHealth(jgen, "enrollment_coordinator", value.enrollmentCoordinator());
      } else {
        if (idStartsWith(jgen, "nca_")) {
          jgen.writeObjectField("fax", null);
        }
        jgen.writeObjectField("main", null);
      }
      jgen.writeEndObject();
    }
  }

  private static final class PropertiesSerializer extends StdSerializer<GeoFacility.Properties> {
    public PropertiesSerializer() {
      this(null);
    }

    public PropertiesSerializer(Class<GeoFacility.Properties> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(
        GeoFacility.Properties value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      jgen.writeObjectField("id", value.id());
      jgen.writeObjectField("name", value.name());
      jgen.writeObjectField("facility_type", value.facilityType());
      jgen.writeObjectField("classification", value.classification());
      jgen.writeObjectField("website", value.website());
      jgen.writeObjectField(
          "address",
          Optional.ofNullable(value.address()).orElse(Facility.Addresses.builder().build()));
      jgen.writeObjectField(
          "phone", Optional.ofNullable(value.phone()).orElse(Facility.Phone.builder().build()));
      jgen.writeObjectField(
          "hours", Optional.ofNullable(value.hours()).orElse(Facility.Hours.builder().build()));
      jgen.writeObjectField(
          "operational_hours_special_instructions", value.operationalHoursSpecialInstructions());
      jgen.writeObjectField(
          "services",
          Optional.ofNullable(value.services()).orElse(Facility.Services.builder().build()));
      jgen.writeObjectField(
          "satisfaction",
          Optional.ofNullable(value.satisfaction())
              .orElse(Facility.Satisfaction.builder().build()));
      jgen.writeObjectField(
          "wait_times",
          Optional.ofNullable(value.waitTimes()).orElse(Facility.WaitTimes.builder().build()));
      jgen.writeObjectField("mobile", value.mobile());
      jgen.writeObjectField("active_status", value.activeStatus());
      jgen.writeObjectField("operating_status", value.operatingStatus());
      jgen.writeObjectField("detailed_services", value.detailedServices());
      jgen.writeObjectField("visn", value.visn());
      jgen.writeEndObject();
    }
  }

  private static final class SatisfactionSerializer extends StdSerializer<Facility.Satisfaction> {
    public SatisfactionSerializer() {
      this(null);
    }

    public SatisfactionSerializer(Class<Facility.Satisfaction> t) {
      super(t);
    }

    private static boolean empty(Facility.Satisfaction value) {
      return value.health() == null && value.effectiveDate() == null;
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.Satisfaction value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value) || idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField(
            "health",
            Optional.ofNullable(value.health())
                .orElse(Facility.PatientSatisfaction.builder().build()));
        jgen.writeObjectField("effective_date", value.effectiveDate());
      }
      jgen.writeEndObject();
    }
  }

  private static final class ServicesSerializer extends StdSerializer<Facility.Services> {
    public ServicesSerializer() {
      this(null);
    }

    public ServicesSerializer(Class<Facility.Services> t) {
      super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.Services value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField("other", Optional.ofNullable(value.other()).orElse(emptyList()));
      } else {
        writeNonNull(jgen, "other", value.other());
      }
      if (idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField("health", Optional.ofNullable(value.health()).orElse(emptyList()));
      } else {
        writeNonNull(jgen, "health", value.health());
      }
      if (idStartsWith(jgen, "vba_") && (value.health() == null || value.health().isEmpty())) {
        jgen.writeObjectField(
            "benefits", Optional.ofNullable(value.benefits()).orElse(emptyList()));
      } else {
        writeNonNull(jgen, "benefits", value.benefits());
      }
      if (value.lastUpdated() != null || idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField("last_updated", value.lastUpdated());
      }
      jgen.writeEndObject();
    }
  }

  private static final class WaitTimesSerializer extends StdSerializer<Facility.WaitTimes> {
    public WaitTimesSerializer() {
      this(null);
    }

    public WaitTimesSerializer(Class<Facility.WaitTimes> t) {
      super(t);
    }

    private static boolean empty(Facility.WaitTimes value) {
      return value.health() == null && value.effectiveDate() == null;
    }

    @Override
    @SneakyThrows
    public void serialize(
        Facility.WaitTimes value, JsonGenerator jgen, SerializerProvider provider) {
      jgen.writeStartObject();
      if (!empty(value) || idStartsWith(jgen, "vha_")) {
        jgen.writeObjectField("health", Optional.ofNullable(value.health()).orElse(emptyList()));
        jgen.writeObjectField("effective_date", value.effectiveDate());
      }
      jgen.writeEndObject();
    }
  }
}
