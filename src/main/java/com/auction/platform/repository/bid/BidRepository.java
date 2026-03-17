package com.auction.platform.repository.bid;

import com.auction.platform.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.auction.platform.model.bid.Bid;
import com.auction.platform.model.bid.BidStatus;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByCustomer(Customer customer);

    List<Bid> findByCustomerId(Long customerId);

    List<Bid> findByStatus(BidStatus status);

    List<Bid> findByItemNameIgnoreCase(String itemName);
}
