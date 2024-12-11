package jobkorea.careerpath.sample.enums;

import common.standard.enums.GenericEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SampleType implements GenericEnum<Integer> {

	NONE(1, "미접수") {
		public String url() {
			return String.format("/%s/%s.com",this.getValue(), this.getDescription()); }
	},
	APPLY(2, "접수") {
	public String url() {
		return String.format("/%s/%s.com",this.getValue(), this.getDescription()); }
},
		APPROVAL(3, "승인") {
public String url() {
		return String.format("/%s/%s.com",this.getValue(), this.getDescription()); }
		};

private Integer value;
	private String description;

	public abstract String url();

	SampleType(Integer value, String description) {
		this.value = value;
		this.description = description;
	}

	@Override
	public Integer getValue() {
		return this.value;
	}

	@Override
	public String getDescription() {
		return this.description;
	}
}
