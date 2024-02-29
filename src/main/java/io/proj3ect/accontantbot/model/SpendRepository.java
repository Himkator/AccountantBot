package io.proj3ect.accontantbot.model;

import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface SpendRepository extends CrudRepository<Spend, Long> {
}
