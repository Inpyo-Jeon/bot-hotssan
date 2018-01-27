package io.coinpeeker.bot_hotssan.model.constant;

public enum CoinType {
    ADA("카르다노", "ada")
    ,BTC("비트코인", "btc")
    ,DENT("덴트", "dent")
    ,ETC("이더리움클래식", "etc")
    ,ETH("이더리움", "eth")
    ,MED("메디블록", "med")
    ,NEO("네오", "neo")
    ,QTUM("퀀텀", "qtum")
    ,SPC("스페이스체인", "spc")
    ,TRX("트론", "trx")
    ,TSL("에너고", "tsl")
    ,XGOX("곡스", "xgox")
    ,XRP("리플", "xrp");



    private String desc;
    private String symbol;


    CoinType(String desc, String symbol) {
        this.desc = desc;
        this.symbol = symbol;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
