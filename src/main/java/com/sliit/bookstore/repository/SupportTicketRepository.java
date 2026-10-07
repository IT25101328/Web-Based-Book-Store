package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.SupportTicket;
import com.sliit.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCustomerOrderByCreatedDateDesc(User customer);

    long countByStatus(SupportTicket.TicketStatus status);
}
