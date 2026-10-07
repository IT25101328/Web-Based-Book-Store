package com.sliit.bookstore.service;

import com.sliit.bookstore.model.SupportMessage;
import com.sliit.bookstore.model.SupportTicket;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.SupportMessageRepository;
import com.sliit.bookstore.repository.SupportTicketRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private SupportMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public List<SupportTicket> getTicketsForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return ticketRepository.findByCustomerOrderByCreatedDateDesc(user);
    }

    @Transactional
    public SupportTicket createTicket(String username, String issueCategory, Long orderId, String description) {
        User user = userRepository.findByUsername(username).orElseThrow();

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(user);
        ticket.setIssueCategory(issueCategory);
        ticket.setOrderId(orderId);
        ticket.setDescription(description);
        ticket.setStatus(SupportTicket.TicketStatus.OPEN);

        ticket = ticketRepository.save(ticket);

        SupportMessage message = new SupportMessage();
        message.setTicket(ticket);
        message.setSenderType(SupportMessage.SenderType.CUSTOMER);
        message.setMessage(description);
        messageRepository.save(message);

        return ticket;
    }

    public SupportTicket getTicketDetails(String username, Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!ticket.getCustomer().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized access to ticket");
        }
        return ticket;
    }

    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdDate"));
    }

    @Transactional
    public SupportTicket updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(SupportTicket.TicketStatus.valueOf(status));
        return ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Ticket not found");
        }
        ticketRepository.deleteById(ticketId);
    }

    public long countOpenTickets() {
        return ticketRepository.countByStatus(SupportTicket.TicketStatus.OPEN);
    }
}
