package api.server.fixedlength.builder;

import api.server.common.constant.GramInfoConstant;
import api.server.fixedlength.enmus.LottecardGuIdType;
import api.server.fixedlength.header.Header;
import api.server.fixedlength.helper.FixedLengthHelper;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.vo.LottecardBatchHeaderVO;

public class LottecardBatchHeaderBuilder implements Header {

    @Override
    public String buildHeader(FixedLengthRequest request) {
        LottecardBatchHeaderVO header = new LottecardBatchHeaderVO();
        header.setGramLnth("200");
        header.setGuid(LottecardGuIdType.VOT.generateWithSuffix("SYSNAME1","120000000"));
        header.setGramPrgNo("00");
        header.setGramNo(request.getGramId());
        header.setAkRspDc(GramInfoConstant.SEND);
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
