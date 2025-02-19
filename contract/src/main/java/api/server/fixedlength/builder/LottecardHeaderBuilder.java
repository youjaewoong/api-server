package api.server.fixedlength.builder;

import api.server.common.constant.GramInfoConstant;
import api.server.fixedlength.enmus.LottecardGuIdType;
import api.server.fixedlength.header.Header;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.vo.LottecardCommonHeaderVO;

public class LottecardHeaderBuilder implements Header {

    @Override
    public String buildHeader(FixedLengthRequest request) {
        LottecardCommonHeaderVO header = new LottecardCommonHeaderVO();

        header.setGramLnth(String.valueOf(request.getInFieldLength()+200));
        header.setGuid(LottecardGuIdType.VOT.generateWithSuffix(GramInfoConstant.SYS_CD_AIC));
        header.setGramNo(request.getGramId()); // 메타 전문번호
        header.setRspBizDc("01");
        header.setGramAkDtti(FixedLengthHelper.getCurrentTimestamp());
        header.setIpAdd("192.168.1.1");
        header.setTgtSvId("SERVICE001");
        header.setDeNatvNo1("123456789");
        header.setDeNatvNo2("987654321");
        header.setOriGrnCn("OriginalContent");
        header.setSmlDeYn(GramInfoConstant.SML_DE_N);
        header.setGramRspDtti("20230101123545000");
        header.setPcRc("0");
        header.setFstErrSysDc("FSERR001");
        header.setChnlHdExsYn("Y");
        header.setExrFld("ExtraFieldData");

        // 고정 길이 문자열로 변환
        return FixedLengthHelper.toFixedLengthString(header);
    }
}
