package io.coinpeeker.bot_hotssan.model;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */


@Entity
@Table(name = "LOTTO_PRICE")
@Getter
public class Prizes implements Serializable {
    private static final long serialVersionUID = -3009157732242241606L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "estimated_prizes")
    private long estimated_prizes;

    @Column(name = "cumulative_sales")
    private long cumulative_sales;

    @Column(name = "timestamp")
    private Timestamp timestamp;

    protected Prizes() {

    }

    public Prizes(long estimated_prizes, long cumulative_sales, Timestamp timestamp) {
        this.estimated_prizes = estimated_prizes;
        this.cumulative_sales = cumulative_sales;
        this.timestamp = timestamp;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public long getId() {
        return this.id;
    }

    // TODO: lombok을 사용해서 Getter 이용할 수 있는 방안 강구
    public long getEstimated_prizes() {
        return this.estimated_prizes;
    }

    public long getCumulative_sales() {
        return this.cumulative_sales;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }
}
