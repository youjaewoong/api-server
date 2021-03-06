package com.api.server.model.memogroup;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemoGroup {
	@NotBlank
	private String id;
	private String title;
	@NotBlank
	@JsonProperty("agent_id")
	private String agentId;
}