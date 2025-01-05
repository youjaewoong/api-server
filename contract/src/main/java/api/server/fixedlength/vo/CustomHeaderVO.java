package api.server.fixedlength.vo;

import api.server.fixedlength.header.HeaderType;
import lombok.Data;

@Data
public class CustomHeaderVO implements HeaderType  {

    private String key;
    private String value;

}
