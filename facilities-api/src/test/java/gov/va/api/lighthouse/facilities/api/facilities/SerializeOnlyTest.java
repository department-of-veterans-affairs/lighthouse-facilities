package gov.va.api.lighthouse.facilities.api.facilities;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.Test;

public class SerializeOnlyTest {
  @Test
  public void all() {
    String path = "/all.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }

  @SneakyThrows
  private <T> void assertReadable(String path, Class<T> clazz) {
    T response = createMapper().readValue(getClass().getResourceAsStream(path), clazz);
    assertThat(response).isExactlyInstanceOf(clazz);
  }

  @Test
  public void nearby() {
    String path = "/nearby.json";
    assertReadable(path, NearbyFacility.class);
  }

  @Test
  @SneakyThrows
  public void readBenefits() {
    String path = "/read-benefits.json";
    assertReadable(path, FacilitiesReadResponse.class);
  }

  @Test
  @SneakyThrows
  public void readBenefitsGeoJson() {
    String path = "/read-benefits-geojson.json";
    assertReadable(path, GeoFacility.class);
  }

  @Test
  @SneakyThrows
  public void readCemetery() {
    String path = "/read-cemetery.json";
    assertReadable(path, FacilitiesReadResponse.class);
  }

  @Test
  @SneakyThrows
  public void readCemeteryGeoJson() {
    String path = "/read-cemetery-geojson.json";
    assertReadable(path, GeoFacility.class);
  }

  @Test
  public void readHealth() {
    String path = "/read-health.json";
    assertReadable(path, FacilitiesReadResponse.class);
  }

  @Test
  public void readHealthGeoJson() {
    String path = "/read-health-geojson.json";
    assertReadable(path, GeoFacility.class);
  }

  @Test
  @SneakyThrows
  public void readStateCemetery() {
    String path = "/read-state-cemetery.json";
    assertReadable(path, FacilitiesReadResponse.class);
  }

  @Test
  @SneakyThrows
  public void readStateCemeteryGeoJson() {
    String path = "/read-state-cemetery-geojson.json";
    assertReadable(path, GeoFacility.class);
  }

  @Test
  public void readVetCenter() {
    String path = "/read-vet-center.json";
    assertReadable(path, FacilitiesReadResponse.class);
  }

  @Test
  public void readVetCenterGeoJson() {
    String path = "/read-vet-center-geojson.json";
    assertReadable(path, GeoFacility.class);
  }

  @Test
  public void searchByBbox() {
    String path = "/search-bbox.json";
    assertReadable(path, FacilitiesSearchResponse.class);
  }

  @Test
  public void searchByBboxGeoJson() {
    String path = "/search-bbox-geojson.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }

  @Test
  public void searchByIds() {
    String path = "/search-ids.json";
    assertReadable(path, FacilitiesSearchResponse.class);
  }

  @Test
  public void searchByIdsGeoJson() {
    String path = "/search-ids-geojson.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }

  @Test
  public void searchByLatLong() {
    String path = "/search-lat-long.json";
    assertReadable(path, FacilitiesSearchResponse.class);
  }

  @Test
  public void searchByLatLongGeoJson() {
    String path = "/search-lat-long-geojson.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }

  @Test
  public void searchByState() {
    String path = "/search-state.json";
    assertReadable(path, FacilitiesSearchResponse.class);
  }

  @Test
  public void searchByStateGeoJson() {
    String path = "/search-state-geojson.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }

  @Test
  public void searchByZip() {
    String path = "/search-zip.json";
    assertReadable(path, FacilitiesSearchResponse.class);
  }

  @Test
  public void searchByZipGeoJson() {
    String path = "/search-zip-geojson.json";
    assertReadable(path, GeoFacilitiesResponse.class);
  }
}