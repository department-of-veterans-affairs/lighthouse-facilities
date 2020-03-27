package gov.va.api.lighthouse.facilities;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SuppressWarnings("WeakerAccess")
@Controller
public class FacilitiesHomeController {
  private final Resource openapi;

  @Autowired
  public FacilitiesHomeController(@Value("classpath:/openapi.json") Resource openapi) {
    this.openapi = openapi;
  }

  /** The OpenAPI specific content in yaml form. */
  @SuppressWarnings("WeakerAccess")
  @Bean
  public String openapiContent() throws IOException {
    try (InputStream is = openapi.getInputStream()) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }

  /** OpenAPI Json. */
  @GetMapping(
      value = {"/", "/docs/v0/api", "/openapi.json"},
      produces = "application/json")
  @ResponseBody
  public Object openapiJson() throws IOException {
    return JacksonConfig.createMapper().readValue(openapiContent(), Object.class);
  }
}