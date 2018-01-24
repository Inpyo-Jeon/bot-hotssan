package io.coinpeeker.bot_hotssan.model.constant;

public enum ExchangeType {

    BINANCE("바이낸스")
    ,BITHUMB("빗썸")
    ,BITTREX("비트렉스")
    ,COINNEST("코인네스트")
    ,COINONE("코인원")
    ,COINRAIL("코인레일")
    ,CRYPTOPIA("크립토피아")
    ,EXX("엑스")
    ,KUCOIN("쿠코인")
    ,UPBIT("업비트");

    private String desc;

    ExchangeType(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return this.desc;
    }
}
