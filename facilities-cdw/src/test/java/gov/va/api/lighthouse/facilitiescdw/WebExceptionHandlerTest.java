package gov.va.api.lighthouse.facilitiescdw;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.Test;

public class WebExceptionHandlerTest {
  @Test
  public void handleSnafu() {
    ErrorResponse response = new WebExceptionHandler().handleSnafu(new RuntimeException("oh noez"));
    assertThat(Instant.now().toEpochMilli() - response.timestamp()).isLessThan(2000);
    response.timestamp(0);
    assertThat(response)
        .isEqualTo(
            ErrorResponse.builder()
                .timestamp(0)
                .type("RuntimeException")
                .message("oh noez")
                .build());
  }
}