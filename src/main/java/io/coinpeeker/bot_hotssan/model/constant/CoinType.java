package io.coinpeeker.bot_hotssan.model.constant;

public enum CoinType {

    BTC("비트코인")
    ,ETH("이더리움")
    ,ETC("이더리움클래식")
    ,DENT("덴트")
    ,MED("메디블록")
    ,TRX("트론")
    ,QTUM("퀀텀")
    ,NEO("네오")
    ,XGOX("곡스")
    ,BCH("비트코인캐시")
    ,XRP("리플");


    private String desc;

    CoinType(String desc){
        this.desc = desc;
    }
    public String getDesc(){
        return this.desc;
    }

}
