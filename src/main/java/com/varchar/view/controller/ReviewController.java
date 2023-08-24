package com.varchar.view.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.varchar.biz.buy.BuyDetailService;
import com.varchar.biz.buy.BuyDetailVO;
import com.varchar.biz.common.AlertVO;
import com.varchar.biz.common.Paging;
import com.varchar.biz.common.PagingVO;
import com.varchar.biz.review.ReviewService;
import com.varchar.biz.review.ReviewVO;
import com.varchar.biz.tea.TeaService;
import com.varchar.biz.tea.TeaVO;

@Controller
public class ReviewController {
	
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private BuyDetailService buyDetailService;
	@Autowired
	private TeaService teaService;
	
	// ------------------------- 리뷰 목록 페이지 ---------------------------------
	
	@RequestMapping(value="/reviewListPage.do")
	public String reviewListPage(ReviewVO reviewVO, HttpServletRequest request, PagingVO pagingVO) {
		
		// pMemberId가 뭐임??????
		String memberId = request.getParameter("pMemberId");
		String searchName = request.getParameter("searchName");
		String reviewSearch = request.getParameter("reviewSearch");
		
		reviewVO.setMemberId(memberId == null ? "" : memberId);
		reviewVO.setSearchName(searchName == null ? "" : searchName);
		reviewVO.setReviewSearch(reviewSearch == null ? "" : reviewSearch);
		
		System.out.println("로그 memberId: " + memberId);
		
		List<ReviewVO> reviewDatasTotal = reviewService.selectAll(reviewVO); // 총 리뷰 개수
		
		pagingVO.setTotalCnt(reviewDatasTotal.size());
		pagingVO.setCurrentPageStr(request.getParameter("page"));
		
		// 페이지네이션 모듈화
		pagingVO = Paging.paging(pagingVO);
			
		request.setAttribute("startPage", pagingVO.getStartPage());
		request.setAttribute("endPage", pagingVO.getEndPage());
		request.setAttribute("currentPage", pagingVO.getCurrentPage());
		request.setAttribute("totalPageCnt", pagingVO.getTotalPageCnt());
		
		request.setAttribute("searchName", searchName);
		request.setAttribute("reviewSearch", reviewSearch);
		request.setAttribute("pMemberId", memberId);

		
		reviewVO.setSearchName(searchName + "_PAGING");
		reviewVO.setStartRnum(pagingVO.getStartRnum());
		List<ReviewVO> reviewDatas = reviewService.selectAll(reviewVO); // startRnum 부터 endRnum 까지의 리뷰
		request.setAttribute("reviewDatas", reviewDatas);
		
		return "reviewList.jsp";
	}
	
	// ------------------------- 리뷰 상세 페이지 ---------------------------------
	
	@RequestMapping(value="/reviewDetailPage.do")
	public String reviewDetailPage(ReviewVO reviewVO, Model model) {
		
		reviewVO.setReviewSearch("리뷰상세");
		reviewVO = reviewService.selectOne(reviewVO);
		System.out.println("로그: reviewDetailAction: " + reviewVO);
		
		if (reviewVO != null) {
			model.addAttribute("reviewData", reviewVO);
		}
		System.out.println(reviewVO);
		return "reviewDetail.jsp";
	}

	// ------------------------- 리뷰 작성 페이지 ---------------------------------
	
	@RequestMapping(value="/insertReviewPage.do")
	public String insertReviewsPage(ReviewVO reviewVO, BuyDetailVO buyDetailVO, TeaVO teaVO, HttpSession session, Model model) {
		
		reviewVO.setMemberId((String)session.getAttribute("sessionMemberId"));
		
		buyDetailVO.setBuySearch("주문상세");
		buyDetailVO = buyDetailService.selectOne(buyDetailVO);
		
		teaVO = teaService.selectOne(teaVO);
		model.addAttribute("reviewData", reviewVO);
		model.addAttribute("teaDatas", teaVO);
		model.addAttribute("buyDetalDatas", buyDetailVO);
		
		System.out.println("InsertReviewsPageAction buySerial 로그: " + buyDetailVO);
		System.out.println("InsertReviewsPageAction buySerial 로그: " + teaVO);
		System.out.println("InsertReviewsPageAction buySerial 로그: " + buyDetailVO.getBuySerial());
		System.out.println("InsertReviewsPageAction buySerial 로그: " + reviewVO);
		
		return "insertReviewPage.jsp";
	}
	
	@RequestMapping(value="/insertReview.do")
	public String insertReviews(ReviewVO reviewVO, HttpSession session, Model model) {
		
		reviewVO.setMemberId((String)session.getAttribute("sessionMemberId"));
		
		if(reviewService.insert(reviewVO)) {
			AlertVO sweetAlertVO = new AlertVO("후기작성", "후기작성완료!", null, "success", "reviewListPage.do?searchName=ALL");
			model.addAttribute("sweetAlert", sweetAlertVO);
		}
		return "alertTrue.jsp";
	}
	
	// ------------------------- 리뷰 수정 페이지 ---------------------------------
	
	@RequestMapping(value="/updateReview.do")
	public String updateReviews(ReviewVO reviewVO, Model model) {
		reviewService.update(reviewVO);
		model.addAttribute("reviewData", reviewService.selectOne(reviewVO));
		return "reviewDetailPage.do";
	}
	
	@RequestMapping(value="/updateReviewPage.do")
	public String updateReviewsPage(ReviewVO reviewVO, Model model) {
		reviewVO.setReviewSearch("리뷰상세");
		reviewVO = reviewService.selectOne(reviewVO);
		System.out.println("로그: UrpAction: " + reviewVO);
		
		if (reviewVO != null) {
			model.addAttribute("reviewData", reviewVO);
		}
		System.out.println(reviewVO);
		return "updateReview.jsp";
	}
	
	// ------------------------- 리뷰 삭제 페이지 ---------------------------------
	
	@RequestMapping(value="/deleteReviewPage.do")
	public String deleteReviewsPage(Model model, ReviewVO reviewVO) {
		
		// int reviewNum = Integer.parseInt(request.getParameter("reviewNum")); 
		
		AlertVO sweetAlertVO = new AlertVO("후기삭제", "후기를 삭제하시겠습니까?", null, "question", "deleteReview.do?reviewNum=" + reviewVO.getReviewNum());
		model.addAttribute("sweetAlert", sweetAlertVO);
		
		return "alertChoose.jsp";
	}

	@RequestMapping(value="/deleteReview.do")
	public String deleteReviews(ReviewVO reviewVO, HttpSession session, Model model) {
		
		reviewVO.setMemberId((String)session.getAttribute("sessionMemberId"));
		
		if(reviewService.delete(reviewVO)) {
			AlertVO sweetAlertVO = new AlertVO("후기삭제", "후기 삭제 성공!", null, "success", "myReviewListPage.do?searchName=MEMBER");
			model.addAttribute("sweetAlert", sweetAlertVO);
		}
		
		return "alertTrue.jsp";
	}
	
	// ------------------------- 내 후기 페이지 ---------------------------------
	
	@RequestMapping(value="/myReviewsListPage.do")
	public String myReviewListPage(ReviewVO reviewVO, HttpSession session, HttpServletRequest request, PagingVO pagingVO) {

		String memberId = (String)session.getAttribute("sessionMemberId");
		reviewVO.setMemberId(memberId);
		System.out.println("마이페이지 리뷰 로그1 memberId " +memberId);
		
		String searchName = request.getParameter("searchName");
		String reviewSearch = request.getParameter("reviewSearch");
		
		reviewVO.setSearchName(searchName == null ? "" : searchName);
		reviewVO.setReviewSearch(reviewSearch == null ? "" : reviewSearch);
		
		List<ReviewVO> reviewDatasTotal = reviewService.selectAll(reviewVO); // 총 리뷰 개수
		pagingVO.setTotalCnt(reviewDatasTotal.size());
		pagingVO.setCurrentPageStr(request.getParameter("page"));
		
		// 페이지네이션 모듈화
		pagingVO = Paging.paging(pagingVO);
		System.out.println("마이페이지 리뷰 로그2 reviewDatasTotal " +reviewDatasTotal);

		
		request.setAttribute("startPage", pagingVO.getStartPage());
		request.setAttribute("endPage", pagingVO.getEndPage());
		request.setAttribute("currentPage", pagingVO.getCurrentPage());
		request.setAttribute("totalPageCnt", pagingVO.getTotalPageCnt());
		
		request.setAttribute("searchName", searchName);
		request.setAttribute("reviewSearch", reviewSearch);
		request.setAttribute("pMemberId", memberId);
		
		reviewVO.setSearchName(searchName + "_PAGING");
		reviewVO.setStartRnum(pagingVO.getStartRnum());
		//
		reviewVO.setMemberId(memberId);
		
		List<ReviewVO> reviewDatas = reviewService.selectAll(reviewVO); // startRnum 부터 endRnum 까지의 리뷰
		System.out.println("마이페이지 리뷰 로그3 reviewDatas " +reviewDatas);
		System.out.println("마이페이지 리뷰 로그4 memberId " +memberId);
		request.setAttribute("reviewDatas", reviewDatas);
		
		return "reviewList.jsp";
	}
	
}
