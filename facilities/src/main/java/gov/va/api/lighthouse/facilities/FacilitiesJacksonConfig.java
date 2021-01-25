package gov.va.api.lighthouse.facilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FacilitiesJacksonConfig {
  static ObjectMapper createMapper() {
    return new FacilitiesJacksonConfig().objectMapper();
  }

  /** Mask away checked exception so this Jackson can be used in streams. */
  @SneakyThrows
  static <T> T quietlyMap(ObjectMapper mapper, InputStream json, Class<T> type) {
    return mapper.readValue(json, type);
  }

  /** Mask away checked exception so this Jackson can be used in streams. */
  @SneakyThrows
  static <T> T quietlyMap(ObjectMapper mapper, String json, Class<T> type) {
    return mapper.readValue(json, type);
  }

  /** Mask away checked exception so this Jackson can be used in streams. */
  @SneakyThrows
  static String quietlyWriteValueAsString(ObjectMapper mapper, Object obj) {

    String test = mapper.writeValueAsString(obj);
    if (test == null) {
      System.out.println("NULL STRING VALUE QUEITLY WRITTEN!!!");
    }

    return test;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return JacksonConfig.createMapper().registerModule(JacksonSerializersV0.serializersV0());
  }
}
