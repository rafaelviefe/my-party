package com.myparty.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	Optional<Event> findByTitle(String title);

}
