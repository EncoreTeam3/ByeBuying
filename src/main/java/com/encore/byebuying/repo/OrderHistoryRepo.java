package com.encore.byebuying.repo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.encore.byebuying.domain.OrderHistory;

import java.util.Date;

public interface OrderHistoryRepo extends JpaRepository<OrderHistory, Long>{
	OrderHistory findOrderHistoryById(Long id);
	Page<OrderHistory> findByUsername(Pageable pageable,String username);
	Page<OrderHistory> findByDateBetweenAndUsername(Pageable pageable, Date start, Date end, String username);
	void deleteAllByUsername(String username);
}
