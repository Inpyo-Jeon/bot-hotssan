package io.coinpeeker.bot_hotssan.controller;

import io.coinpeeker.bot_hotssan.model.Prizes;
import io.coinpeeker.bot_hotssan.repository.PrizesRepository;
import io.coinpeeker.bot_hotssan.service.lotto.PriceProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */

@Controller
public class SearchController {

    @Autowired
    @Qualifier("prizesRepository")
    PrizesRepository prizesRepository;

    @Value("${data.pageDivisionNum}")
    int pageDivisionNum;

    @Value("${data.displayPostNum}")
    int displayPostNum;

    // TODO: 페이징처리가 꼭 new 방식이어야하는가?
    @RequestMapping(value = {"/lotto"}, method = RequestMethod.GET, produces = "text/html")
    public String find(@RequestParam(defaultValue = "1", required = false) int currentPageNum, Model model) throws IOException {

//        Pageable pageable = PageRequest.of(currentPageNum - 1, displayPostNum, new Sort(Sort.Direction.DESC, "timestamp"));
        Pageable pageable = new PageRequest(currentPageNum - 1, displayPostNum, new Sort(Sort.Direction.DESC, "timestamp"));

        Paging paging = new Paging(displayPostNum, pageDivisionNum, (int) prizesRepository.count(), currentPageNum);

        List<String> prizesStringList = new ArrayList<>();
        prizesRepository.findAll(pageable).forEach(item -> {
            prizesStringList.add(convertString(item));
        });

        model.addAttribute("prizesStringList", prizesStringList);
        model.addAttribute("isPre", paging.isPre());
        model.addAttribute("isNext", paging.isNext());
        model.addAttribute("pageStart", paging.getPageStartNum());
        model.addAttribute("pageEnd", paging.getPageEndNum());
        return "list";
    }

    private String convertString(Prizes prizes) {
        StringBuilder sb = new StringBuilder();
        sb.append("기준시간 : ")
                .append(convertTime(prizes.getTimestamp().getTime()))
                .append(" -- ")
                .append("1등 당첨 금액 : ")
                .append(convertUnit(prizes.getEstimated_prizes()))
                .append(" -- ")
                .append("누적 당첨 금액 : ")
                .append(convertUnit(prizes.getCumulative_sales()))
                .append("\n");

        return sb.toString();
    }

    private String convertTime(long time) {
        Date date = new Date();
        date.setTime(time);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private String convertUnit(long money) {
        return String.format("%,d", money);
    }



    class Paging {
        private int displayPostNum = 0;
        private int pageDivisionNum = 0;
        private int totalPostNum = 0;
        private int currentPageNum = 0;

        public Paging(int displayPostNum, int pageDivisionNum, int totalPostNum, int currentPageNum) {
            this.displayPostNum = displayPostNum;
            this.pageDivisionNum = pageDivisionNum;
            this.totalPostNum = totalPostNum;
            this.currentPageNum = currentPageNum;
        }

        public int getTotalPostNum() {
            return ((this.totalPostNum - 1) / this.displayPostNum) + 1;
        }

        public int getPageStartNum() {
            return ((this.currentPageNum - 1) / this.pageDivisionNum) * this.pageDivisionNum + 1;
        }

        public int getPageEndNum() {
            return Math.min(getPageStartNum() + this.pageDivisionNum - 1, getTotalPostNum());
        }

        public boolean isPre() {
            return getPageStartNum() != 1;
        }

        public boolean isNext() {
            return getPageEndNum() < getTotalPostNum();
        }

        public int getPostRangeStartNum() {
            return getPostRangeEndNum() - this.displayPostNum + 1;
        }

        public int getPostRangeEndNum() {
            return this.currentPageNum * this.displayPostNum;
        }
    }
}