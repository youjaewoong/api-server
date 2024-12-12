package api.server.sample.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "샘플 외부데이터 응답")
public class SampleFeignResponse {

	private int id;
	private String name;
	private String username;
	private String email;
	private SampleAddressInfoResponse address;
	private String phone;
	private String website;
	private SampleCompanyInfoResponse company;

}
