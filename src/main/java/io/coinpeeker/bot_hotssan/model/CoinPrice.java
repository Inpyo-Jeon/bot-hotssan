package io.coinpeeker.bot_hotssan.model;

import io.coinpeeker.bot_hotssan.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CoinPrice {

    @Autowired
    CommonUtils commonUtils;

    private String key;
    private String exchangeName;
    private String krw;
    private String usd;
    private String satoshi;

    public CoinPrice(String key, String exchangeName) {
        this.key = key;
        this.exchangeName = exchangeName;
    }

    public void setKrw(String krw) {
        this.krw = krw;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public void setSatoshi(String satoshi) {
        this.satoshi = satoshi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(this.exchangeName);
        sb.append("]");

        if (this.satoshi != null) {
            sb.append("\n사토시 : ");
            sb.append(CommonUtils.convertSatoshi(this.satoshi));
        }

        if (this.usd != null) {
            sb.append("\n달러 : ");
            sb.append(CommonUtils.convertUSD(this.usd));
        }

        if (this.krw != null) {
            sb.append("\n원화 : ");
            sb.append(CommonUtils.convertKRW(this.krw));
        }

        return sb.toString();
    }
}
