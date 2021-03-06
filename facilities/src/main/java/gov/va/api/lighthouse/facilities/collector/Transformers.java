package gov.va.api.lighthouse.facilities.collector;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class Transformers {

  /** Check if all values are blank. */
  public static boolean allBlank(Object... values) {
    for (Object v : values) {
      if (!isBlank(v)) {
        return false;
      }
    }
    return true;
  }

  static String checkAngleBracketNull(String s) {
    if ("<Null>".equalsIgnoreCase(s)) {
      return null;
    } else {
      return s;
    }
  }

  static <T> List<T> emptyToNull(List<T> items) {
    if (isEmpty(items)) {
      return null;
    }
    List<T> filtered = items.stream().filter(Objects::nonNull).collect(toList());
    return filtered.isEmpty() ? null : filtered;
  }

  static String hoursToClosed(String hours) {
    String trim = trimToNull(hours);
    if (equalsIgnoreCase(trim, "-")) {
      return "Closed";
    }
    return trim;
  }

  /** Check if an object is blank. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    }
    if (value instanceof Optional<?>) {
      return ((Optional<?>) value).isEmpty() || isBlank(((Optional<?>) value).get());
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<?, ?>) value).isEmpty();
    }
    return value == null;
  }

  static String phoneTrim(String phone) {
    String trim = trimToNull(phone);

    if (endsWithIgnoreCase(trim, "x")) {
      trim = trimToNull(trim.substring(0, trim.length() - 1));
    }

    if ("000-000-0000".equalsIgnoreCase(trim)) {
      return null;
    }

    return trim;
  }
}
