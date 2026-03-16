package com.auction.platform.bid;

import com.auction.platform.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByCustomer(Customer customer);

    List<Bid> findByStatus(BidStatus status);

    List<Bid> findByItemNameIgnoreCase(String itemName);
}
