package com.myparty.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.Event;
import com.myparty.app.entities.User;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
