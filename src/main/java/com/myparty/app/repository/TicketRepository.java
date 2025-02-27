package com.myparty.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.Event;
import com.myparty.app.entities.Ticket;
import com.myparty.app.entities.User;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

	boolean existsByUserAndEventAndStatusNot(User user, Event event, Ticket.Status status);

	Optional<Ticket> findByUserAndEventAndStatus(User user, Event event, Ticket.Status status);

	@Query(value = """
    SELECT COALESCE(SUM(
        CASE\s
            WHEN tu.is_student = true THEN te.price / 2
            ELSE te.price
        END
    ), 0)
    FROM tb_tickets t
    JOIN tb_users tu ON t.user_id = tu.user_id
    JOIN tb_events te ON t.event_id = te.event_id
    WHERE t.status = 'APPROVED' AND te.event_id = :eventId
   \s""", nativeQuery = true)
	Double calculateRevenueByEvent(@Param("eventId") Long eventId);

}
