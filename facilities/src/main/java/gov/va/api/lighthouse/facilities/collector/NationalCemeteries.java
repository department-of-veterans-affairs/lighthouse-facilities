package gov.va.api.lighthouse.facilities.collector;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JacksonXmlRootElement(localName = "cems")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NationalCemeteries {
  @Builder.Default
  @JacksonXmlElementWrapper(useWrapping = false)
  public List<NationalCemetery> cem = new ArrayList<>();

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static final class NationalCemetery {
    @JacksonXmlProperty(isAttribute = true, localName = "station")
    String id;

    @JacksonXmlProperty(isAttribute = true, localName = "cem_name")
    String name;

    @JacksonXmlProperty(isAttribute = true, localName = "cem_url")
    String url;

    // Unused fields:
    // lat
    // long
    // statecode
    // address_line1
    // address_line2
    // address_line3
    // mailing_line1
    // mailing_line2
    // mailing_line3
    // phone
    // fax
    // contact1
    // contact2
    // email
    // fac_id
    // status
  }
}
