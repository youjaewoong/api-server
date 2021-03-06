package com.api.server.model.bookmark;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchBookmarkRequest {
	
	@JsonProperty("agent_id")
	@NotBlank
	private String agentId;
	
	@JsonProperty("group_id")
	private String groupId;
	
	@JsonProperty("target_id")
	private String targetId;
}
