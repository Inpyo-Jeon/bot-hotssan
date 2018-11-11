package io.coinpeeker.bot_hotssan.repository;

import io.coinpeeker.bot_hotssan.model.Prizes;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */

public interface PrizesRepository extends CrudRepository<Prizes, Long> {
    Iterable<Prizes> findAll(Pageable request);


}
