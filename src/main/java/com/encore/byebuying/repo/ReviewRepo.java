package com.encore.byebuying.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.encore.byebuying.domain.Review;

import java.util.Date;

public interface ReviewRepo extends JpaRepository<Review, Long>{
	Review findReviewById(Long id);
	Page<Review> findByItemid(Pageable pageable,Long itemid);

	Page<Review> findAll(Pageable pageable);
	Page<Review> findByDateBetween(Pageable pageable, Date start, Date end);

	Page<Review> findByItemnameContainingIgnoreCase(Pageable pageable, String itemname);
	Page<Review> findByDateBetweenAndItemnameContainingIgnoreCase(Pageable pageable, Date start, Date end, String itemname);

	Page<Review> findByUsername(Pageable pageable, String username);
	Page<Review> findByDateBetweenAndUsername(Pageable pageable, Date start, Date end, String username);

	Page<Review> findByUsernameAndItemnameContainingIgnoreCase(Pageable pageable, String username, String itemname);
	Page<Review> findByDateBetweenAndUsernameAndItemnameContainingIgnoreCase(Pageable pageable, Date start, Date end, String username, String itemname);

    @Query(value="select avg(score) from review where itemname like :keyword1", nativeQuery=true )
	Double getAvgScoreByItemname(@Param("keyword1") String itemname);
    @Query(value="select count(score) from review where itemname like :keyword1", nativeQuery=true )
   	int CountScoreByItemname(@Param("keyword1") String itemname);

	void deleteAllByUsername(String username);
}
