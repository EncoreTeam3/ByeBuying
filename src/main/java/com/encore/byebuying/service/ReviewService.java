package com.encore.byebuying.service;

import com.encore.byebuying.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.encore.byebuying.domain.Review;

import java.text.ParseException;

public interface ReviewService {
	Page<Review> getReviews(Pageable pageable); // 전체 리뷰 조회
	Page<Review> getReviews(Pageable pageable, String start, String end) throws ParseException; // 날짜별 조회
	Page<Review> getUsername(Pageable pageable, String username);
	Page<Review> getUsername(Pageable pageable, String start, String end, String username) throws ParseException;
	Page<Review> getItemname(Pageable pageable, String itemname);
	Page<Review> getItemname(Pageable pageable, String start, String end, String itemname) throws ParseException;
	Page<Review> getUserNItem(Pageable pageable, String username, String itemname);
	Page<Review> getUserNItem(Pageable pageable, String start, String end, String username, String itemname) throws ParseException;

	Page<Review> getByItemid(Pageable pageable,Long itemid); // itemid 기준 리뷰 조회


	Review saveReview(Review review); // 리뷰 저장
	Review getReview(Long id); // 리뷰 1개 조회
	void deleteReviewById(Long id);
	String getAvgScoreByItemname(String itemname);
	int countScoreByItemname(String itemname);
}
