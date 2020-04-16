package gov.va.api.lighthouse.facilities;

import static java.util.Collections.emptySet;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface FacilityRepository
    extends CrudRepository<FacilityEntity, FacilityEntity.Pk>,
        JpaSpecificationExecutor<FacilityEntity> {
  @Query("select e.id from #{#entityName} e")
  List<FacilityEntity.Pk> findAllIds();

  List<HasFacilityPayload> findAllProjectedBy();

  List<FacilityEntity> findByIdIn(Collection<FacilityEntity.Pk> ids);

  @Value
  @Builder
  final class BBoxSpecification implements Specification<FacilityEntity> {
    @NonNull BigDecimal minLongitude;

    @NonNull BigDecimal maxLongitude;

    @NonNull BigDecimal minLatitude;

    @NonNull BigDecimal maxLatitude;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<Facility.ServiceType> services = emptySet();

    @Override
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(5);
      basePredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("longitude"), minLongitude));
      basePredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("longitude"), maxLongitude));
      basePredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("latitude"), minLatitude));
      basePredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("latitude"), maxLatitude));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }
      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      Predicate[] servicePredicates =
          services.stream()
              .map(svc -> criteriaBuilder.isMember(svc.toString(), root.get("services")))
              .toArray(Predicate[]::new);
      Predicate anyService = criteriaBuilder.or(servicePredicates);
      return criteriaBuilder.and(combinedBase, anyService);
    }
  }

  @Value
  @Builder
  final class StateSpecification implements Specification<FacilityEntity> {
    @NonNull String state;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<Facility.ServiceType> services = emptySet();

    @Override
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(2);
      basePredicates.add(criteriaBuilder.equal(root.get("state"), state));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }
      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      Predicate[] servicePredicates =
          services.stream()
              .map(svc -> criteriaBuilder.isMember(svc.toString(), root.get("services")))
              .toArray(Predicate[]::new);
      Predicate anyService = criteriaBuilder.or(servicePredicates);
      return criteriaBuilder.and(combinedBase, anyService);
    }
  }

  @Value
  @Builder
  final class StationNumbersSpecification implements Specification<FacilityEntity> {
    @Builder.Default Set<String> stationNumbers = emptySet();

    FacilityEntity.Type facilityType;

    @Builder.Default Set<Facility.ServiceType> services = emptySet();

    @Override
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      if (isEmpty(stationNumbers)) {
        return criteriaBuilder.isTrue(criteriaBuilder.literal(false));
      }

      List<Predicate> basePredicates = new ArrayList<>(2);

      In<String> stationsInClause = criteriaBuilder.in(root.get("id").get("stationNumber"));
      for (String stationNumber : stationNumbers) {
        stationsInClause.value(stationNumber);
      }
      basePredicates.add(stationsInClause);

      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }
      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      Predicate[] servicePredicates =
          services.stream()
              .map(svc -> criteriaBuilder.isMember(svc.toString(), root.get("services")))
              .toArray(Predicate[]::new);
      Predicate anyService = criteriaBuilder.or(servicePredicates);
      return criteriaBuilder.and(combinedBase, anyService);
    }
  }

  @Value
  @Builder
  final class TypeServicesOnlySpecification implements Specification<FacilityEntity> {
    FacilityEntity.Type facilityType;

    @Builder.Default Set<Facility.ServiceType> services = emptySet();

    @Override
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(1);
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }
      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      Predicate[] servicePredicates =
          services.stream()
              .map(svc -> criteriaBuilder.isMember(svc.toString(), root.get("services")))
              .toArray(Predicate[]::new);
      Predicate anyService = criteriaBuilder.or(servicePredicates);
      return criteriaBuilder.and(combinedBase, anyService);
    }
  }

  @Value
  @Builder
  final class ZipSpecification implements Specification<FacilityEntity> {
    @NonNull String zip;

    FacilityEntity.Type facilityType;

    @Builder.Default Set<Facility.ServiceType> services = emptySet();

    @Override
    public Predicate toPredicate(
        Root<FacilityEntity> root,
        CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
      List<Predicate> basePredicates = new ArrayList<>(2);
      basePredicates.add(criteriaBuilder.equal(root.get("zip"), zip));
      if (facilityType != null) {
        basePredicates.add(criteriaBuilder.equal(root.get("id").get("type"), facilityType));
      }
      Predicate combinedBase = criteriaBuilder.and(basePredicates.toArray(new Predicate[0]));
      if (isEmpty(services)) {
        return combinedBase;
      }
      Predicate[] servicePredicates =
          services.stream()
              .map(svc -> criteriaBuilder.isMember(svc.toString(), root.get("services")))
              .toArray(Predicate[]::new);
      Predicate anyService = criteriaBuilder.or(servicePredicates);
      return criteriaBuilder.and(combinedBase, anyService);
    }
  }
}
