package com.myparty.app.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	Optional<Event> findByTitle(String title);

	boolean existsByTitleAndEventIdNot(String title, Long eventId);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.eventId = :eventId and t.status = 'APPROVED'")
	Long countTicketsByEventId(@Param("eventId") Long eventId);

	List<Event> findByDateBetween(Instant start, Instant end);
}
