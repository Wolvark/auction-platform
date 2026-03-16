package com.auction.platform.auction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findByItemId(Long itemId);

    List<Auction> findByStatus(AuctionStatus status);

    Optional<Auction> findFirstByItemIdAndStatusNot(Long itemId, AuctionStatus status);
}
