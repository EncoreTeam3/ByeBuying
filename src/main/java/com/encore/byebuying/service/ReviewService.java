package com.encore.byebuying.service;

import com.encore.byebuying.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.encore.byebuying.domain.Review;

import java.text.ParseException;

public interface ReviewService {
	Page<Review> getReviews(Pageable pageable); // 전체 리뷰 조회 // 필요하려나
	Review getReview(Long id); // 리뷰 1개 조회
	Review saveReview(Review review); // 리뷰 저장
	Page<Review> getByItemid(Pageable pageable,Long itemid); // itemid 기준 리뷰 조회
	Page<Review> getByUsername(Pageable pageable,String username); // username 기준 리뷰 조회
	Page<Review> getByUsernameAndBetweenDate(Pageable pageable, String start, String end, String username) throws ParseException;
	
	void deleteReviewById(Long id);
	String getAvgScoreByItemname(String itemname);
	int countScoreByItemname(String itemname);
}
