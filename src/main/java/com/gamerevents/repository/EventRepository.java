package com.gamerevents.repository;

import com.gamerevents.model.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByStartDateAsc();

    List<Event> findByFeaturedTrueOrderByStartDateAsc();

    @Query("select e from Event e "
            + "where (:category is null or lower(e.category) = lower(:category)) "
            + "and (:location is null or lower(e.location) like lower(concat('%', :location, '%'))) "
            + "and (:startDate is null or e.startDate >= :startDate) "
            + "and (:endDate is null or e.startDate <= :endDate) "
            + "order by e.startDate asc")
    List<Event> search(@Param("category") String category,
                       @Param("location") String location,
                       @Param("startDate") LocalDateTime startDate,
                       @Param("endDate") LocalDateTime endDate);

    @Query("select distinct e from Event e left join fetch e.games where e.id = :id")
    Optional<Event> findDetailedById(@Param("id") Long id);
}
