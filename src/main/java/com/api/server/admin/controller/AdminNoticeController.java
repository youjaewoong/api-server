package com.api.server.admin.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.server.admin.model.notice.AdminNoticeDeptResponse;
import com.api.server.admin.model.notice.AdminNoticeResponse;
import com.api.server.admin.model.notice.CreateAdminNotice;
import com.api.server.admin.model.notice.DeleteAdminNotice;
import com.api.server.admin.model.notice.SearchAdminNoticeRequest;
import com.api.server.admin.model.notice.UpdateAdminNotice;
import com.api.server.admin.service.AdminNoticeService;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/advisor/admin")
public class AdminNoticeController {

	private final AdminNoticeService adminNoticeService;
	
	@ApiOperation("조회")
    @GetMapping("notices")
    public List<AdminNoticeResponse> selectAdminNotices(@NotBlank @RequestParam("admin_id") String adminId) {
		
		SearchAdminNoticeRequest searchAdminNoticeRequest = new SearchAdminNoticeRequest();
		searchAdminNoticeRequest.setAdminId(adminId);
        return adminNoticeService.selectAdminNotices(searchAdminNoticeRequest);
    }
	
	
	@ApiOperation("추가")
    @PostMapping("notice")
    public void createAdminNotice(@RequestBody CreateAdminNotice createAdminNotice) {
		adminNoticeService.createAdminNotice(createAdminNotice);
    }
	  
    
    @ApiOperation("단건 수정")
    @PutMapping("/notices/{id}")
    public void updateAdminNotice(@PathVariable String id, @RequestBody UpdateAdminNotice updateAdminNotice) {
    	updateAdminNotice.setId(id);
    	adminNoticeService.updateAdminNotice(updateAdminNotice);
    }
    
    
	@ApiOperation("단건 삭제")
    @DeleteMapping("notices/{id}")
    public void deleteAdminNotice(@PathVariable String id, @Valid @RequestBody DeleteAdminNotice deleteAdminNotice) {
		deleteAdminNotice.setId(id);
		adminNoticeService.deleteAdminNotice(deleteAdminNotice);
    }
	
	
	@ApiOperation("부서 조회")
	@GetMapping("notices/dept")
    public List<AdminNoticeDeptResponse> selectAdminNoticeDept(@NotBlank @RequestParam("company_code") String companyCode) {
		return adminNoticeService.selectAdminNoticeDept(companyCode);
    }
	
	
}