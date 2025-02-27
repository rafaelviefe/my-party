package com.myparty.app.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findBySendAtBeforeAndSentFalse(Instant now);
}
